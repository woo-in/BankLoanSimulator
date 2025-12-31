package bankapp.loan.origination.service;

import bankapp.loan.common.component.InterestRateCalculator;
import bankapp.loan.exceptions.InvalidPendingLoan;
import bankapp.loan.origination.component.DsrCalculator;
import bankapp.loan.origination.model.ExistingLoan;
import bankapp.loan.origination.model.PendingLoanApplication;
import bankapp.loan.origination.model.PendingLoanApplicationStatus;
import bankapp.loan.origination.repository.PendingLoanApplicationRepository;
import bankapp.loan.origination.web.request.ApplicationAuthRequest;
import bankapp.loan.origination.web.request.CreditCheckRequest;
import bankapp.loan.origination.web.request.FinancialInfoRequest;
import bankapp.loan.origination.web.response.ApplicationResponse;
import bankapp.loan.origination.web.response.ExistingLoanResponse;
import bankapp.loan.product.model.LoanProduct;
import bankapp.loan.origination.web.response.InterestRateInfoResponse;
import bankapp.loan.origination.web.request.ApplicationRequest;
import bankapp.loan.product.service.InterestRateTypeService;
import bankapp.loan.product.service.LoanProductService;
import bankapp.loan.product.service.RepaymentMethodService;
import bankapp.loan.underwriting.service.LoanApplicationService;
import bankapp.member.exceptions.IncorrectPasswordException;
import bankapp.member.exceptions.MemberNotFoundException;
import bankapp.member.model.Member;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DefaultLoanOriginationService implements LoanOriginationService{


    // todo : refactor


    private final PendingLoanApplicationRepository pendingLoanApplicationRepository;
    private final LoanProductService loanProductService;
    private final RepaymentMethodService repaymentMethodService;
    private final InterestRateTypeService interestRateTypeService;
    private final LoanContractService loanContractService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final InterestRateCalculator interestRateCalculator;
    private final LoanApplicationService loanApplicationService;
    private final DsrCalculator dsrCalculator;


    @Autowired
    public DefaultLoanOriginationService(PendingLoanApplicationRepository pendingLoanApplicationRepository,
                                         LoanProductService loanProductService,
                                         RepaymentMethodService repaymentMethodService,
                                         InterestRateTypeService interestRateTypeService,
                                         LoanContractService loanContractService,
                                         PasswordEncoder passwordEncoder,
                                         ObjectMapper objectMapper,
                                         InterestRateCalculator interestRateCalculator,
                                         LoanApplicationService loanApplicationService,
                                         DsrCalculator dsrCalculator) {
        this.pendingLoanApplicationRepository = pendingLoanApplicationRepository;
        this.loanProductService = loanProductService;
        this.repaymentMethodService = repaymentMethodService;
        this.interestRateTypeService = interestRateTypeService;
        this.loanContractService = loanContractService;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.interestRateCalculator = interestRateCalculator;
        this.loanApplicationService = loanApplicationService;
        this.dsrCalculator = dsrCalculator;
    }


    @Override
    @Transactional
    public Long startOrigination(Member member,
                                 String productSlug,
                                 FinancialInfoRequest userInfoRequest,
                                 List<ExistingLoanResponse> allExistingLoans) {

        if (member == null) {
            throw new MemberNotFoundException("회원 정보가 유효하지 않습니다.");
        }

        LoanProduct loanProduct = loanProductService.findByLoanProductSlug(productSlug);

        PendingLoanApplication draftApp = PendingLoanApplication.builder()
                .member(member)
                .loanProduct(loanProduct)
                .status(PendingLoanApplicationStatus.FINANCIAL_INFO_SUBMITTED)
                .totalAssets(userInfoRequest.getTotalAssetsAmount())
                .annualIncome(userInfoRequest.getAnnualIncomeAmount())
                .fixedExpenses(userInfoRequest.getFixedExpensesAmount())
                .build();

        if (allExistingLoans != null && !allExistingLoans.isEmpty()) {
            for (ExistingLoanResponse dto : allExistingLoans) {

                ExistingLoan existingLoanEntity = ExistingLoan.builder()
                        .loanProductName(dto.getLoanProductName())
                        .loanType(dto.getLoanType() != null ? dto.getLoanType() : "신용대출")
                        .loanAmount(dto.getLoanAmount())
                        .remainingBalance(dto.getRemainingBalance())
                        .loanTerm(dto.getLoanTerm())
                        .repaymentMethodName(dto.getRepaymentMethodName())
                        .interestRateTypeName(dto.getInterestRateTypeName())
                        .totalInterestRate(dto.getTotalInterestRate())
                        .isExternal(dto.isExternal())
                        .build();

                existingLoanEntity.setPendingLoanApplication(draftApp);
            }
        }

        PendingLoanApplication savedApp = pendingLoanApplicationRepository.save(draftApp);
        return savedApp.getPendingLoanApplicationId();
    }


    @Override
    @Transactional
    public void selectLoanTerms(Long pendingLoanApplicationId, ApplicationRequest request) {


        PendingLoanApplication pendingApp = pendingLoanApplicationRepository.findById(pendingLoanApplicationId)
                .orElseThrow(() -> new InvalidPendingLoan("신청 정보를 찾을 수 없습니다."));



        pendingApp.setRequestLoanAmount(request.getLoanAmount());
        pendingApp.setRequestLoanTerm(request.getLoanTerm());
        pendingApp.setRepaymentMethod(repaymentMethodService.findByMethodName(request.getRepaymentMethod()));
        pendingApp.setInterestRateType(interestRateTypeService.findByTypeName(request.getInterestRateType()));

        pendingApp.setStatus(PendingLoanApplicationStatus.TERMS_SELECTED);

    }

    @Override
    @Transactional
    public void completeOrigination(Long pendingLoanApplicationId , ApplicationAuthRequest applicationAuthRequest){

        // 1. 비밀번호 확인
        PendingLoanApplication pendingApp = pendingLoanApplicationRepository.findById(pendingLoanApplicationId)
                .orElseThrow(() -> new InvalidPendingLoan("신청 정보를 찾을 수 없습니다."));
        Member loginMember = pendingApp.getMember();
        if(!passwordEncoder.matches(applicationAuthRequest.getPassword(), loginMember.getPassword())) {
            throw new IncorrectPasswordException("비밀번호가 일치하지 않습니다.");
        }

        // 2. pendingLoanApplication 바탕으로 LoanApplication 만들기
        loanApplicationService.saveLoanApplication(pendingApp);

        // 3. pendingLoanApplication 상태 신청 완료로 수정
        pendingApp.setStatus(PendingLoanApplicationStatus.COMPLETED);
    }

    @Override
    @Transactional(readOnly = true)
    public PendingLoanApplicationStatus getApplicationStatus(Long pendingLoanApplicationId){

        PendingLoanApplication pendingApp = pendingLoanApplicationRepository.findById(pendingLoanApplicationId)
                .orElseThrow(() -> new InvalidPendingLoan("신청 정보를 찾을 수 없습니다."));

        return pendingApp.getStatus();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExistingLoanResponse> getIntegratedLoanList(Member member, FinancialInfoRequest request) {

        // 1. [내부 부채] 우인은행 대출 조회
        List<ExistingLoanResponse> internalLoans = loanContractService.findAllContractResponsesByMember(member);

        // 2. [외부 부채] JSON 파싱
        List<ExistingLoanResponse> externalLoans = new ArrayList<>();
        try {
            String json = request.getExternalLoansJson();
            if (json != null && !json.isBlank() && !json.equals("[]")) {
                externalLoans = objectMapper.readValue(json, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("타행 대출 정보 파싱 실패. 타행 대출을 제외하고 계산을 진행합니다.", e);
        }

        List<ExistingLoanResponse> allLoans = new ArrayList<>();
        allLoans.addAll(internalLoans);
        allLoans.addAll(externalLoans);

        return allLoans;
    }

    @Override
    public BigDecimal calculateTotalDebt(List<ExistingLoanResponse> loans) {
        if (loans == null || loans.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return loans.stream()
                .map(ExistingLoanResponse::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public InterestRateInfoResponse calculateInterestRate(String productSlug,
                                                          FinancialInfoRequest userInfoRequest,
                                                          List<ExistingLoanResponse> allExistingLoans) {
        BigDecimal totalDebtAmount = calculateTotalDebt(allExistingLoans);
        CreditCheckRequest creditCheckRequest = CreditCheckRequest.from(userInfoRequest, totalDebtAmount);
        return interestRateCalculator.calculateInterestRateInfo(productSlug, creditCheckRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public InterestRateInfoResponse calculateInterestRate(String productSlug, Long pendingLoanApplicationId) {

        PendingLoanApplication pendingApp = pendingLoanApplicationRepository.findById(pendingLoanApplicationId)
                .orElseThrow(() -> new InvalidPendingLoan("진행 중인 대출 신청 정보를 찾을 수 없습니다. ID: " + pendingLoanApplicationId));

        FinancialInfoRequest userInfoRequest = new FinancialInfoRequest();
        userInfoRequest.setTotalAssetsAmount(pendingApp.getTotalAssets());
        userInfoRequest.setAnnualIncomeAmount(pendingApp.getAnnualIncome());
        userInfoRequest.setFixedExpensesAmount(pendingApp.getFixedExpenses());

        List<ExistingLoanResponse> existingLoanResponses = pendingApp.getExistingLoans().stream()
                .map(ExistingLoanResponse::from)
                .toList(); // Java 16+ (혹은 .collect(Collectors.toList()))


        BigDecimal totalDebtAmount = calculateTotalDebt(existingLoanResponses);
        CreditCheckRequest creditCheckRequest = CreditCheckRequest.from(userInfoRequest, totalDebtAmount);
        return interestRateCalculator.calculateInterestRateInfo(productSlug, creditCheckRequest);
    }


    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationResponse(Long pendingLoanApplicationId){
        PendingLoanApplication pendingApp = pendingLoanApplicationRepository.findById(pendingLoanApplicationId)
                .orElseThrow(() -> new InvalidPendingLoan("진행 중인 대출 신청 정보를 찾을 수 없습니다. ID: " + pendingLoanApplicationId));

        return ApplicationResponse.builder()
                .loanAmount(pendingApp.getRequestLoanAmount())
                .loanTerm(pendingApp.getRequestLoanTerm())
                .repaymentMethod(pendingApp.getRepaymentMethod().getMethodName())
                .interestRateType(pendingApp.getInterestRateType().getTypeName())
                .build();

    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getSelectionSpread(Long pendingLoanApplicationId){
        PendingLoanApplication pendingApp = pendingLoanApplicationRepository.findById(pendingLoanApplicationId)
                .orElseThrow(() -> new InvalidPendingLoan("진행 중인 대출 신청 정보를 찾을 수 없습니다. ID: " + pendingLoanApplicationId));

        return interestRateCalculator.calculateSelectionSpread(pendingApp);
    }

    @Override
    @Transactional
    public void setRate(Long pendingLoanApplicationId){
        PendingLoanApplication pendingApp = pendingLoanApplicationRepository.findById(pendingLoanApplicationId)
                .orElseThrow(() -> new InvalidPendingLoan("진행 중인 대출 신청 정보를 찾을 수 없습니다. ID: " + pendingLoanApplicationId));

        InterestRateInfoResponse interestRateInfoResponse = calculateInterestRate(pendingApp.getLoanProduct().getLoanProductSlug(),pendingLoanApplicationId);

        BigDecimal selectionSpread = getSelectionSpread(pendingLoanApplicationId);
        BigDecimal baseRate = interestRateInfoResponse.getBaseRate();
        BigDecimal productSpread = interestRateInfoResponse.getProductSpread();
        BigDecimal creditSpread = interestRateInfoResponse.getCreditSpread();
        BigDecimal finalInterestRate = BigDecimal.ZERO.add(selectionSpread)
                                                      .add(baseRate)
                                                      .add(productSpread)
                                                      .add(creditSpread);

        pendingApp.setSelectionSpread(selectionSpread);
        pendingApp.setBaseRate(baseRate);
        pendingApp.setProductSpread(productSpread);
        pendingApp.setCreditSpread(creditSpread);
        pendingApp.setFinalInterestRate(finalInterestRate);

        pendingLoanApplicationRepository.save(pendingApp);
    }

    @Override
    @Transactional
    public void setDsr(Long pendingLoanApplicationId){
        PendingLoanApplication pendingApp = pendingLoanApplicationRepository.findById(pendingLoanApplicationId)
                .orElseThrow(() -> new InvalidPendingLoan("진행 중인 대출 신청 정보를 찾을 수 없습니다. ID: " + pendingLoanApplicationId));

        pendingApp.setDebtServiceRatio(dsrCalculator.calculate(pendingApp));
        pendingLoanApplicationRepository.save(pendingApp);
    }


}
