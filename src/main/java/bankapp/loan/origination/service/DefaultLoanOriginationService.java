package bankapp.loan.origination.service;

import bankapp.loan.common.component.InterestRateCalculator;
import bankapp.loan.exceptions.InvalidLoanProduct;
import bankapp.loan.origination.model.ApplicationStatus;
import bankapp.loan.origination.model.ExistingLoan;
import bankapp.loan.origination.model.PendingLoanApplication;
import bankapp.loan.origination.repository.PendingLoanApplicationRepository;
import bankapp.loan.origination.web.request.CreditCheckRequest;
import bankapp.loan.origination.web.request.UserFinancialInfoRequest;
import bankapp.loan.origination.web.response.ExistingLoanResponse;
import bankapp.loan.product.model.LoanProduct;
import bankapp.loan.product.repository.LoanProductRepository;
import bankapp.loan.origination.web.response.InterestRateInfoResponse;
import bankapp.member.exceptions.MemberNotFoundException;
import bankapp.member.model.Member;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DefaultLoanOriginationService implements LoanOriginationService{

    private final PendingLoanApplicationRepository pendingLoanApplicationRepository;
    private final LoanProductRepository loanProductRepository;
    private final LoanContractService loanContractService;
    private final ObjectMapper objectMapper;
    private final InterestRateCalculator interestRateCalculator;


    @Autowired
    public DefaultLoanOriginationService(PendingLoanApplicationRepository pendingLoanApplicationRepository,
                                         LoanProductRepository loanProductRepository,
                                         LoanContractService loanContractService,
                                         ObjectMapper objectMapper,
                                         InterestRateCalculator interestRateCalculator) {
        this.pendingLoanApplicationRepository = pendingLoanApplicationRepository;
        this.loanProductRepository = loanProductRepository;
        this.loanContractService = loanContractService;
        this.objectMapper = objectMapper;
        this.interestRateCalculator = interestRateCalculator;
    }


    @Override
    @Transactional
    public void startOrigination(Member member,
                                 String productSlug,
                                 UserFinancialInfoRequest userInfoRequest,
                                 List<ExistingLoanResponse> allExistingLoans) {

        if (member == null) {
            throw new MemberNotFoundException("회원 정보가 유효하지 않습니다.");
        }

        LoanProduct loanProduct = loanProductRepository.findByLoanProductSlug(productSlug)
                .orElseThrow(() -> new InvalidLoanProduct("존재하지 않는 대출 상품입니다. slug: " + productSlug));

        PendingLoanApplication draftApp = PendingLoanApplication.builder()
                .member(member)
                .loanProduct(loanProduct)
                .status(ApplicationStatus.DRAFT)
                .totalAssets(userInfoRequest.getTotalAssetsAmount())
                .annualIncome(userInfoRequest.getAnnualIncomeAmount())
                .fixedExpenses(userInfoRequest.getFixedExpensesAmount())
                .build();

        // 4. [핵심] 기존 대출 정보(DTO) -> 엔티티 변환 및 연관관계 설정
        if (allExistingLoans != null && !allExistingLoans.isEmpty()) {
            for (ExistingLoanResponse dto : allExistingLoans) {

                // todo : 타행 , 당행을 이름으로 판단 (수정 필요)
                ExistingLoan existingLoanEntity = ExistingLoan.builder()
                        .loanProductName(dto.getLoanProductName())
                        .loanType(dto.getLoanType() != null ? dto.getLoanType() : "신용대출") // Null 방지
                        .loanAmount(dto.getLoanAmount())
                        .loanTerm(dto.getLoanTerm())
                        .repaymentMethodName(dto.getRepaymentMethodName())
                        .totalInterestRate(dto.getTotalInterestRate())
                        .isExternal(!dto.getLoanProductName().contains("우인"))
                        .build();

                existingLoanEntity.setPendingLoanApplication(draftApp);
            }
        }

        pendingLoanApplicationRepository.save(draftApp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExistingLoanResponse> getIntegratedLoanList(Member member, UserFinancialInfoRequest request) {

        // 1. [내부 부채] 우인은행 대출 조회
        List<ExistingLoanResponse> internalLoans = loanContractService.findAllContractResponsesByMember(member);

        // 2. [외부 부채] JSON 파싱
        List<ExistingLoanResponse> externalLoans = new ArrayList<>();
        try {
            String json = request.getExternalLoansJson();
            if (json != null && !json.isBlank() && !json.equals("[]")) {
                externalLoans = objectMapper.readValue(json, new TypeReference<List<ExistingLoanResponse>>() {});
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
                                                          UserFinancialInfoRequest userInfoRequest,
                                                          List<ExistingLoanResponse> allExistingLoans) {
        BigDecimal totalDebtAmount = calculateTotalDebt(allExistingLoans);
        CreditCheckRequest creditCheckRequest = CreditCheckRequest.from(userInfoRequest, totalDebtAmount);
        return interestRateCalculator.calculateInterestRateInfo(productSlug, creditCheckRequest);
    }




}
