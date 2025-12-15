package bankapp.loan.common.component;

import bankapp.infra.client.bok.BokApiClient;
import bankapp.infra.client.bok.BokInterestRateDto;
import bankapp.loan.exceptions.BaseRateFetchException;
import bankapp.loan.exceptions.LoanProductNotFoundException;
import bankapp.loan.origination.component.LoanInquiryScorer;
import bankapp.loan.product.service.CreditLoanProductService;
import bankapp.loan.web.request.CreditCheckRequest;
import bankapp.loan.web.response.InterestRateInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class InterestRateCalculator {


    private final BokApiClient bokApiClient;
    private final CreditLoanProductService creditLoanProductService;
    private final LoanInquiryScorer loanInquiryScorer;


    @Autowired
    public InterestRateCalculator(BokApiClient bokApiClient ,
                                  CreditLoanProductService creditLoanProductService,
                                  LoanInquiryScorer loanInquiryScorer) {
        this.bokApiClient = bokApiClient;
        this.creditLoanProductService = creditLoanProductService;
        this.loanInquiryScorer = loanInquiryScorer;
    }

    /**
     * 기준 금리, 상품 가산 금리, 신용 가산 금리를 각각 산출하고
     * 최종 금리까지 합산하여 상세 금리 정보를 반환합니다.
     *
     * @param loanProductSlug 대출 상품 식별자
     * @param request 고객 신용 정보가 포함된 요청 DTO
     * @return 금리 구성 요소(A, B, C)와 최종 금리가 포함된 응답 객체
     * @throws LoanProductNotFoundException 상품을 찾을 수 없는 경우
     */
    public InterestRateInfoResponse calculateInterestRateInfo(String loanProductSlug, CreditCheckRequest request) throws LoanProductNotFoundException {
        // 1. (A) 기준 금리 조회
        BigDecimal baseRate = calculateBaseRate();

        // 2. (B) 상품 가산 금리 조회
        BigDecimal productSpread = calculateProductSpread(loanProductSlug);

        // 3. (C) 신용 가산 금리 산출
        BigDecimal creditSpread = calculateCreditSpread(request);

        // 4. (Final) 최종 금리 합산 (A + B + C)
        BigDecimal finalRate = baseRate.add(productSpread).add(creditSpread);

        // 5. 응답 객체 생성 및 반환
        return new InterestRateInfoResponse(baseRate, productSpread, creditSpread, finalRate);
    }


    /**
     * 기준 금리 계산하여 반환
     */
    public BigDecimal calculateBaseRate(){
        try{
            LocalDate today = LocalDate.of(2025,11,7);
            final String KORIBOR_12M_CODE = "010152000";

            BokInterestRateDto.SearchData responseData = bokApiClient.fetchDayInterestRate(
                    "kr",
                    today,
                    KORIBOR_12M_CODE
            ).block(); // 동기식 대기

            if (responseData == null || responseData.getDataValue() == null || responseData.getDataValue().isEmpty()) {
                throw new BaseRateFetchException("한국은행 API로부터 기준금리(KORIBOR 12M)를 가져오지 못했습니다. (오늘자 데이터 없음) - 날짜: " + today);
            }

            return new BigDecimal(responseData.getDataValue());

        }catch (WebClientResponseException | NumberFormatException e) {
            throw new BaseRateFetchException("한국은행 API 호출 또는 데이터 변환 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            throw new BaseRateFetchException("기준금리 조회 중 알 수 없는 시스템 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 상품 가산 금리 반환
     */
    public BigDecimal calculateProductSpread(String loanProductSlug) throws LoanProductNotFoundException {
        return creditLoanProductService.findCreditLoanProductSpreadByLoanProductSlug(loanProductSlug);
    }

    /**
     * 신용 가산 금리 반환
     */
    public BigDecimal calculateCreditSpread(CreditCheckRequest request) {
        return loanInquiryScorer.getCreditSpread(request);
    }












}
