package bankapp.loan.service.credit;

import bankapp.infra.client.bok.BokApiClient;
import bankapp.infra.client.bok.BokInterestRateDto;
import bankapp.loan.common.enums.FinancialGrade;
import bankapp.loan.exceptions.*;
import bankapp.loan.model.common.rate.InterestRateType;
import bankapp.loan.model.common.rate.LoanProductInterestRateTypeOption;
import bankapp.loan.model.common.repayment.LoanProductRepaymentOption;
import bankapp.loan.model.common.repayment.RepaymentMethod;
import bankapp.loan.model.credit.CreditLoanProduct;
import bankapp.loan.repository.common.rate.LoanProductInterestRateTypeOptionRepository;
import bankapp.loan.repository.common.repayment.LoanProductRepaymentOptionRepository;
import bankapp.loan.repository.credit.CreditLoanProductRepository;
import bankapp.loan.service.common.rate.InterestRateTypeService;
import bankapp.loan.service.common.repayment.RepaymentMethodService;
import bankapp.loan.web.request.LoanProductRequest;
import bankapp.loan.web.response.InterestRateInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DefaultCreditLoanService implements CreditLoanService {


    private final CreditLoanProductRepository creditLoanProductRepository;
    private final LoanProductInterestRateTypeOptionRepository loanProductInterestRateTypeOptionRepository;
    private final LoanProductRepaymentOptionRepository loanProductRepaymentOptionRepository;
    private final InterestRateTypeService interestRateTypeService;
    private final RepaymentMethodService repaymentMethodService;
    private final BokApiClient bokApiClient;



    @Autowired
    public DefaultCreditLoanService(CreditLoanProductRepository creditLoanProductRepository,
                                    LoanProductInterestRateTypeOptionRepository loanProductInterestRateTypeOptionRepository,
                                    LoanProductRepaymentOptionRepository  loanProductRepaymentOptionRepository,
                                    InterestRateTypeService interestRateTypeService,
                                    RepaymentMethodService repaymentMethodService,
                                    BokApiClient bokApiClient) {
        this.creditLoanProductRepository = creditLoanProductRepository;
        this.loanProductInterestRateTypeOptionRepository = loanProductInterestRateTypeOptionRepository;
        this.loanProductRepaymentOptionRepository = loanProductRepaymentOptionRepository;
        this.interestRateTypeService = interestRateTypeService;
        this.repaymentMethodService = repaymentMethodService;
        this.bokApiClient = bokApiClient;
    }


    @Override
    public List<CreditLoanProduct> findAllCreditLoanProducts() {
        return creditLoanProductRepository.findAll();
    }

    @Override
    @Transactional
    public void saveCreditLoanProduct(LoanProductRequest loanProductRequest){

        if(!loanProductRequest.getLoanType().equals("CREDIT")){
            throw new IllegalArgumentException("Invalid loan type: Expected 'CREDIT' but received '" +
                    loanProductRequest.getLoanType() + "'.");
        }

        CreditLoanProduct product = (CreditLoanProduct) loanProductRequest.toEntity();
        CreditLoanProduct savedProduct = creditLoanProductRepository.save(product);

        List<RepaymentMethod> validMethods = repaymentMethodService.findAllById(loanProductRequest.getRepaymentMethodIds());

        if (validMethods.size() != loanProductRequest.getRepaymentMethodIds().size()) {
            throw new InvalidRepaymentMethodId("유효하지 않은 상환 방법 ID가 포함되어 있습니다.");
        }

        List<LoanProductRepaymentOption> repaymentOptions = new ArrayList<>();
        for (RepaymentMethod method : validMethods) {
            LoanProductRepaymentOption option = new LoanProductRepaymentOption();
            option.setLoanProduct(savedProduct);
            option.setRepaymentMethod(method);
            repaymentOptions.add(option);
        }
        loanProductRepaymentOptionRepository.saveAll(repaymentOptions);


        // (위와 동일한 로직)
        List<InterestRateType> validTypes = interestRateTypeService.findAllById(loanProductRequest.getInterestRateTypeIds());

        if (validTypes.size() != loanProductRequest.getInterestRateTypeIds().size()) {
            throw new InvalidInterestRateTypeId("유효하지 않은 금리 유형 ID가 포함되어 있습니다.");
        }

        List<LoanProductInterestRateTypeOption> interestOptions = new ArrayList<>();
        for (InterestRateType type : validTypes) {
            LoanProductInterestRateTypeOption option = new LoanProductInterestRateTypeOption();
            option.setLoanProduct(savedProduct);
            option.setInterestRateType(type);
            interestOptions.add(option);
        }
        loanProductInterestRateTypeOptionRepository.saveAll(interestOptions);

    }

    @Override
    public CreditLoanProduct findCreditLoanProductByLoanProductSlug(String loanProductSlug) throws LoanProductNotFoundException {

        return creditLoanProductRepository.findByLoanProductSlug(loanProductSlug)
                .orElseThrow(() -> new LoanProductNotFoundException("해당 신용대출 상품을 찾을 수 없습니다 : " + loanProductSlug));
    }


    // todo : 임시로 최종금리 계산하는 함수
    // todo : 당연히 상환방식 , 이자율에 따라 바뀌기는 해야 함
    @Override
    @Transactional
    public InterestRateInfoResponse calculateInterestRate(FinancialGrade financialGrade , String loanProductSlug)throws LoanProductNotFoundException ,BaseRateFetchException{

        // 1. 상품마다 금리
        CreditLoanProduct creditLoanProduct = findCreditLoanProductByLoanProductSlug(loanProductSlug);
        BigDecimal productSpread = creditLoanProduct.getDefaultSpread();
        log.info("상품마다 금리 : {}" , productSpread);

        // 2. FinancialGrade 마다 금리
        BigDecimal creditSpread = switch (financialGrade) {
            case SECURE -> new BigDecimal("1.0");   // 최우수
            case STABLE -> new BigDecimal("2.0");   // 우수
            case STANDARD -> new BigDecimal("3.0"); // 보통
            case CAUTION -> new BigDecimal("4.0");  // 주의
            case RISK -> new BigDecimal("5.0");     // 위험
        };
        log.info("신용등급마다 금리 : {}" , creditSpread);

        // 3. 기본금리
        BigDecimal baseRate;
        try{
            LocalDate today = LocalDate.now();
            final String KORIBOR_12M_CODE = "010152000";

            BokInterestRateDto.SearchData responseData = bokApiClient.fetchDayInterestRate(
                    "kr",
                    today,
                    KORIBOR_12M_CODE
            ).block(); // 동기식 대기

            if (responseData == null || responseData.getDataValue() == null || responseData.getDataValue().isEmpty()) {
                throw new BaseRateFetchException("한국은행 API로부터 기준금리(KORIBOR 12M)를 가져오지 못했습니다. (오늘자 데이터 없음) - 날짜: " + today);
            }

            baseRate = new BigDecimal(responseData.getDataValue());

        }catch (WebClientResponseException | NumberFormatException e) {
            throw new BaseRateFetchException("한국은행 API 호출 또는 데이터 변환 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            throw new BaseRateFetchException("기준금리 조회 중 알 수 없는 시스템 오류가 발생했습니다: " + e.getMessage());
        }
        log.info("기본 금리 : {}" , baseRate);


        // 4. 모두 합하여 반환 (기준금리 + 상품가산금리 + 신용가산금리)
        return new InterestRateInfoResponse(baseRate,productSpread,creditSpread,baseRate.add(productSpread).add(creditSpread));
    }




}
