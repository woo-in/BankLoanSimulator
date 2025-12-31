package bankapp.loan.origination.component;

import bankapp.loan.common.component.InterestRateCalculator;
import bankapp.loan.origination.model.ExistingLoan;
import bankapp.loan.origination.model.PendingLoanApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
public class DsrCalculator {

    private final InterestRateCalculator interestRateCalculator;

    @Autowired
    public DsrCalculator(InterestRateCalculator interestRateCalculator){
        this.interestRateCalculator = interestRateCalculator;
    }

    // DSR 산정 만기 (신용대출 만기일시상환 등: 현행 5년 간주)
    private static final BigDecimal DEEMED_TERM_YEARS = new BigDecimal("5");
    // 스트레스 DSR 적용 기준 금액 (1억 원)
    private static final BigDecimal STRESS_DSR_THRESHOLD = new BigDecimal("100000000");


    // todo : 반드시 기존금리들이 모두 계산된 후 , pendingApp 에 저장된 후 호출
    /**
     * 스트레스 DSR 계산
     *
     * @param pendingApp  진행 중인 대출 신청 정보 (기존 대출 및 신청 조건 포함)
     * @return 계산된 DSR 값 (%)
     */
    public BigDecimal calculate(PendingLoanApplication pendingApp) {

        BigDecimal annualIncome = pendingApp.getAnnualIncome();
        if (annualIncome.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // 1. 총 신용대출 잔액 계산 (기존 신용대출 + 신규 신청금액)
        BigDecimal totalCreditLoanBalance = calculateTotalBalance(pendingApp);

        // 2. 스트레스 금리 산출 (조건부 적용)
        BigDecimal stressRateToAdd = BigDecimal.ZERO;

        // "신용대출 전체 잔액이 1억원을 초과하는 경우에 한해 적용"
        if (totalCreditLoanBalance.compareTo(STRESS_DSR_THRESHOLD) > 0) {
            BigDecimal baseStressRate = interestRateCalculator.calculateStressRate();
            stressRateToAdd = applyStressRateRules(baseStressRate, pendingApp);
        }

        // 3. 연간 상환액 합계 계산
        BigDecimal totalAnnualPayment = BigDecimal.ZERO;

        // 3-1. 기존 대출 상환액
        for (ExistingLoan loan : pendingApp.getExistingLoans()) {
            totalAnnualPayment = totalAnnualPayment.add(calculateExistingAnnualPayment(loan));
        }


        // 3-2. 신규 대출 상환액 (스트레스 금리 반영)
        BigDecimal effectiveRate = pendingApp.getFinalInterestRate().add(stressRateToAdd);
        BigDecimal newLoanPayment = calculateNewLoanAnnualPayment(pendingApp, effectiveRate);

        totalAnnualPayment = totalAnnualPayment.add(newLoanPayment);

        // 4. DSR 계산: (총 연상환액 / 연소득) * 100
        return totalAnnualPayment.divide(annualIncome, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 총 대출 잔액 계산 (기존 대출 + 신청 금액)
     */
    private BigDecimal calculateTotalBalance(PendingLoanApplication app) {
        BigDecimal totalBalance = BigDecimal.ZERO;

        if (app.getRequestLoanAmount() != null) {
            totalBalance = totalBalance.add(app.getRequestLoanAmount());
        }

        List<ExistingLoan> existingLoans = app.getExistingLoans();

        if (existingLoans != null) {
            for (ExistingLoan loan : existingLoans) {
                if (loan == null) {
                    continue;
                }
                BigDecimal remainingBalance = loan.getRemainingBalance();
                    if (remainingBalance != null) {
                        totalBalance = totalBalance.add(remainingBalance);
                    }
                }
            }

        return totalBalance;
    }

    /**
     * 스트레스 금리 적용 규칙 (만기 및 금리 유형에 따른 가중치)
     */
    private BigDecimal applyStressRateRules(BigDecimal baseStressRate, PendingLoanApplication app) {
        boolean isFixedRate = isFixedRate(app);
        int termMonths = app.getRequestLoanTerm();

        // -> 만기 5년(60개월) 이상 고정 / 적용 안함 (0%)
        if (isFixedRate && termMonths >= 60) {
            return BigDecimal.ZERO;
        }
        // -> 만기 3~5년(36~59개월) 고정 / 60% 적용
        else if (isFixedRate && termMonths >= 36) {
            return baseStressRate.multiply(new BigDecimal("0.6"));
        }
        // -> 그 외 (변동금리 또는 3년 미만 고정) / 100% 적용
        else {
            return baseStressRate;
        }
    }

    /**
     * 신규 대출 연 상환액 계산 (PMT 등 공식 적용)
     */
    private BigDecimal calculateNewLoanAnnualPayment(PendingLoanApplication app, BigDecimal effectiveRate) {
        BigDecimal principal = app.getRequestLoanAmount();
        Integer termMonths = app.getRequestLoanTerm();
        // 연이율 -> 월이율 변환 (백분율 처리 포함)
        BigDecimal monthlyRate = effectiveRate.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP)
                .divide(new BigDecimal("12"), 8, RoundingMode.HALF_UP);

        // RepaymentMethod 엔티티나 코드를 확인하여 분기 처리
        // (여기서는 PendingLoanApplication의 객체 구조상 메서드명을 가져온다고 가정)
        String methodName = "";
        if (app.getRepaymentMethod() != null) {
            methodName = app.getRepaymentMethod().getMethodName(); // 또는 getMethodCode()
        }

        // 1. 만기일시상환 (BULLET)
        if (methodName.contains("BULLET") || methodName.contains("만기")) {
            // (원금 / 5년) + (원금 * 연이율)
            BigDecimal annualPrincipal = principal.divide(DEEMED_TERM_YEARS, 2, RoundingMode.HALF_UP);
            BigDecimal annualInterest = principal.multiply(effectiveRate.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP));
            return annualPrincipal.add(annualInterest);
        }

        // 2. 원리금 균등 (PMT) -> 1년치(x12)
        if (methodName.contains("EQUAL_PRINCIPAL_INTEREST") || methodName.contains("원리금")) {
            return calculatePmt(principal, monthlyRate, termMonths).multiply(new BigDecimal("12"));
        }

        // 3. 원금 균등 -> (원금/년수) + (평균이자)
        if (methodName.contains("EQUAL_PRINCIPAL") || methodName.contains("원금")) {
            BigDecimal termYears = new BigDecimal(termMonths).divide(new BigDecimal("12"), 4, RoundingMode.HALF_UP);
            BigDecimal annualPrincipal = principal.divide(termYears, 2, RoundingMode.HALF_UP);
            // 약식 평균 이자: 원금 * 이자율 * 0.5
            BigDecimal avgInterest = principal.multiply(effectiveRate.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP))
                    .multiply(new BigDecimal("0.5"));
            return annualPrincipal.add(avgInterest);
        }

        // 기본값 (안전하게 만기일시 로직 적용)
        BigDecimal annualPrincipal = principal.divide(DEEMED_TERM_YEARS, 2, RoundingMode.HALF_UP);
        BigDecimal annualInterest = principal.multiply(effectiveRate.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP));
        return annualPrincipal.add(annualInterest);
    }

    /**
     * 기존 대출 연 상환액 계산 (BriefDsrCalculator 로직 재사용)
     */
    private BigDecimal calculateExistingAnnualPayment(ExistingLoan loan) {
        BigDecimal principal = loan.getLoanAmount();
        BigDecimal rate = loan.getTotalInterestRate().divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);

        // 기존 대출은 '만기일시'인 경우 스트레스 DSR 산정 시에도 5년 분할 상환으로 간주하는 것이 일반적임
        String method = loan.getRepaymentMethodName();

        if (method.contains("만기") || method.contains("BULLET")) {
            BigDecimal annualPrincipal = principal.divide(DEEMED_TERM_YEARS, 2, RoundingMode.HALF_UP);
            BigDecimal annualInterest = principal.multiply(rate);
            return annualPrincipal.add(annualInterest);
        }

        return principal.multiply(rate).add(principal.divide(DEEMED_TERM_YEARS, 2, RoundingMode.HALF_UP));
    }

    // PMT 공식 (원리금균등 월 상환액)
    private BigDecimal calculatePmt(BigDecimal principal, BigDecimal monthlyRate, Integer termMonths) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(new BigDecimal(termMonths), 2, RoundingMode.HALF_UP);
        }
        BigDecimal onePlusRatePow = BigDecimal.ONE.add(monthlyRate).pow(termMonths);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRatePow);
        BigDecimal denominator = onePlusRatePow.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    // [Helper] 고정금리 여부 확인
    private boolean isFixedRate(PendingLoanApplication app) {
        if (app.getInterestRateType() == null) return false;
        String typeName = app.getInterestRateType().getTypeName();
        String typeCode = app.getInterestRateType().getTypeCode();
        return typeName != null && (typeCode.contains("BULLET") || typeName.contains("고정금리"));
    }


}