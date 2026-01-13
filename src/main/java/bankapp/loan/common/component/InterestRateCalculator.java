package bankapp.loan.common.component;

import bankapp.infra.client.bok.BokApiClient;
import bankapp.infra.client.bok.BokInterestRateDto;
import bankapp.loan.exceptions.BaseRateFetchException;
import bankapp.loan.exceptions.LoanProductNotFoundException;
import bankapp.loan.origination.component.LoanInquiryScorer;
import bankapp.loan.origination.component.SelectionSpreadCalculator;
import bankapp.loan.origination.model.PendingLoanApplication;
import bankapp.loan.product.model.RepaymentMethod;
import bankapp.loan.product.service.CreditLoanProductService;
import bankapp.loan.origination.web.request.CreditCheckRequest;
import bankapp.loan.origination.web.response.InterestRateInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class InterestRateCalculator {


    private final BokApiClient bokApiClient;
    private final CreditLoanProductService creditLoanProductService;
    private final LoanInquiryScorer loanInquiryScorer;
    private final SelectionSpreadCalculator selectionSpreadCalculator;


    // 연체 가산 금리 (현재 정책: 2.0%)
    private static final BigDecimal DELINQUENCY_PENALTY_SPREAD = new BigDecimal("2.0");
    // 법정 최고 금리 제한 (현재 정책: 15.0%)
    private static final BigDecimal MAX_LEGAL_INTEREST_RATE = new BigDecimal("15.0");
// ---------------------------------------------------------
    // todo : 임시 설정 입니다. 실제 (대출신청금/대출기간/상환방법/금리종류) 테이블을 등록하면 , 수치를 바꿔야 합니다.
    private static final BigDecimal MIN_SELECTION_SPREAD = new BigDecimal("0.00");
    private static final BigDecimal MAX_SELECTION_SPREAD = new BigDecimal("0.50");

    @Autowired
    public InterestRateCalculator(BokApiClient bokApiClient ,
                                  CreditLoanProductService creditLoanProductService,
                                  LoanInquiryScorer loanInquiryScorer,
                                  SelectionSpreadCalculator selectionSpreadCalculator) {
        this.bokApiClient = bokApiClient;
        this.creditLoanProductService = creditLoanProductService;
        this.loanInquiryScorer = loanInquiryScorer;
        this.selectionSpreadCalculator = selectionSpreadCalculator;
    }

    /**
     * 기준 금리, 상품 가산 금리, 신용 가산 금리를 각각 산출하고
     * 최종 금리까지 합산하여 상세 금리 정보를 반환합니다.
     *
     * @param loanProductSlug 대출 상품 식별자
     * @param request 고객 신용 정보가 포함된 요청 DTO
     * @return 금리 정보가 포함된 응답 객체
     * @throws LoanProductNotFoundException 상품을 찾을 수 없는 경우
     */
    public InterestRateInfoResponse calculateInterestRateInfo(String loanProductSlug, CreditCheckRequest request) throws LoanProductNotFoundException {
        BigDecimal baseRate = calculateBaseRate();
        BigDecimal productSpread = calculateProductSpread(loanProductSlug);
        BigDecimal creditSpread = calculateCreditSpread(request);

        return new InterestRateInfoResponse(
                baseRate,
                productSpread,
                creditSpread,
                MIN_SELECTION_SPREAD,
                MAX_SELECTION_SPREAD
        );
    }


    /**
     * 기준 금리 계산하여 반환
     */
    public BigDecimal calculateBaseRate() {
        // 1. 오늘 날짜부터 조회 시작
        LocalDate targetDate = LocalDate.now();
        final String KORIBOR_12M_CODE = "010152000";

        // 2. 최대 15일 전까지 조회 (명절 + 공휴일 + 주말이 겹치는 최장 연휴 대비)
        int maxRetryDays = 15;

        for (int i = 0; i < maxRetryDays; i++) {
            try {
                BokInterestRateDto.SearchData responseData = bokApiClient.fetchDayInterestRate(
                        "kr",
                        targetDate,
                        KORIBOR_12M_CODE
                ).block(); // 동기식 호출

                // 3. 유효한 데이터가 있으면 즉시 반환
                if (responseData != null && responseData.getDataValue() != null && !responseData.getDataValue().isEmpty()) {
                    return new BigDecimal(responseData.getDataValue());
                }

            } catch (WebClientResponseException e) {
                // API 호출 실패(4xx, 5xx) 시 로그만 남기고 다음 날짜(과거) 시도
                // log.warn("금리 조회 실패 (API 에러) - 날짜: {}", targetDate);
            } catch (Exception e) {
                // 기타 오류 무시하고 계속 시도
            }

            // 4. 데이터가 없으면 '하루 전'으로 날짜 이동
            targetDate = targetDate.minusDays(1);
        }

        // 5. 15일을 다 뒤져도 없으면 예외 발생 (심각한 시스템 오류 또는 한국은행 데이터 누락)
        throw new BaseRateFetchException("최근 " + maxRetryDays + "일 간의 기준금리 데이터를 찾을 수 없습니다. (한국은행 API 확인 필요)");
    }


    /**
     * 가중치 미적용 스트레스 금리(Stress Rate) 반환
     * 공식: (과거 5년 최고 금리 - 현재 금리)
     * - 하한 1.5%, 상한 3.0% 적용
     * 데이터가 없으면 , 안전한 값(0.75) 반환 , 규제상 5월 ,11월 이지만 , 최신으로 현재금리 계산
     */
    public BigDecimal calculateStressRate() {

        final String CREDIT_LOAN_CODE = "BECBLA03051";
        LocalDate now = LocalDate.now();
        // 한국은행 데이터는 집계 시차(1~2개월)가 있으므로 안전하게 2달 전부터 과거 5년을 조회
        LocalDate endDate = now.minusMonths(1);
        LocalDate startDate = endDate.minusYears(5);
        String startMonth = startDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        String endMonth = endDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));

        try {

            List<BokInterestRateDto.SearchData> rates = bokApiClient.fetchMonthlyInterestRates(
                    "kr",
                    startMonth,
                    endMonth,
                    CREDIT_LOAN_CODE
            ).block();

            if (rates == null || rates.isEmpty()) {
                // 데이터가 없으면 안전값(Safe Fallback) 반환
                // 예: 1.5% * 50% = 0.75%
                return new BigDecimal("0.75");
            }

//            log.info(rates.toString());

            // 4. 데이터 파싱
            List<BigDecimal> rateValues = rates.stream()
                    .map(data -> new BigDecimal(data.getDataValue()))
                    .toList();

            // 5. [A] 최고 금리 (Max)
            BigDecimal maxRate = rateValues.stream()
                    .max(BigDecimal::compareTo)
                    .orElseThrow();

            // 6. [B] 현재 금리 - 리스트의 마지막 데이터 사용
            // (규정상 5월/11월 갱신이지만, 시스템에서는 최신 데이터를 쓰는 것이 더 안전함)
            BigDecimal currentRate = rateValues.get(rateValues.size() - 1);

            // 7. 스트레스 금리 원본 계산 (A - B)
            BigDecimal rawStressRate = maxRate.subtract(currentRate);

            // 8. 하한(1.5%) ~ 상한(3.0%) 적용 (Clamping)
            BigDecimal floor = new BigDecimal("1.5");
            BigDecimal ceiling = new BigDecimal("3.0");

            BigDecimal clampedRate;
            if (rawStressRate.compareTo(floor) < 0) {
                clampedRate = floor;
            } else if (rawStressRate.compareTo(ceiling) > 0) {
                clampedRate = ceiling;
            } else {
                clampedRate = rawStressRate;
            }

            return clampedRate;

        } catch (Exception e) {
            // API 장애 시 기본값 반환 (로그 남김)
            log.error("스트레스 금리 계산 중 오류 발생", e);
            return new BigDecimal("0.75");
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

    /**
     * 리스크 감면 금리 반환
     */
    public BigDecimal calculateSelectionSpread(PendingLoanApplication pendingLoanApplication){
        return selectionSpreadCalculator.calculate(pendingLoanApplication);
    }

    /**
     * 1차 연체 금리를 계산하여 반환합니다.
     * 공식: 약정 금리(Applied Rate) + 연체 가산 금리(Penalty Spread)
     * 단, 최종 금리는 법정 최고 금리(15%)를 초과할 수 없습니다.
     *
     * @param appliedRate 약정 금리 (단위: %, 예: 5.4)
     * @return 연체 적용 금리 (단위: %, 예: 7.4)
     */
    public BigDecimal calculatePenaltyRate(BigDecimal appliedRate) {
        if (appliedRate == null) {
            return BigDecimal.ZERO;
        }

        // 1. 연체 금리 계산 (약정 금리 + 가산 금리 2%)
        BigDecimal calculatedRate = appliedRate.add(DELINQUENCY_PENALTY_SPREAD);

        // 2. 법정 최고 금리(15%) 초과 여부 확인 (Clamping)
        // calculatedRate가 MAX 보다 크면(1), MAX를 반환하고 아니면 calculatedRate 반환
        if (calculatedRate.compareTo(MAX_LEGAL_INTEREST_RATE) > 0) {
            return MAX_LEGAL_INTEREST_RATE;
        }

        return calculatedRate;
    }
















}
