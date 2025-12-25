package bankapp.loan.origination.component;

import bankapp.loan.origination.web.request.UserFinancialInfoRequest;
import bankapp.loan.origination.web.response.ExistingLoanResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
public class BriefDsrCalculator {

    // DSR 계산 시 신용대출 만기일시상환의 원금 산정 기간 (현행 규제: 5년)
    private static final BigDecimal DEEMED_TERM_YEARS = new BigDecimal("5");

    public BigDecimal calculateDsr(UserFinancialInfoRequest userInfoRequest, List<ExistingLoanResponse> allLoans) {

        for(ExistingLoanResponse loan : allLoans){
            log.info("loan : " + loan.getLoanProductName() + " , " + loan.getLoanAmount() + " , " + loan.getLoanTerm() + " , " + loan.getRepaymentMethodName() + " , " + loan.getTotalInterestRate());
        }



        BigDecimal annualIncome = userInfoRequest.getAnnualIncomeAmount();

        if (annualIncome.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalAnnualPayment = allLoans.stream()
                .map(this::calculateAnnualPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAnnualPayment.divide(annualIncome, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
    private BigDecimal calculateAnnualPayment(ExistingLoanResponse loan) {
        BigDecimal principal = loan.getLoanAmount();
        BigDecimal rate = loan.getTotalInterestRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        Integer termMonths = loan.getLoanTerm(); // 실제 계약 기간
        String method = loan.getRepaymentMethodName();

        // 1. [수정] 만기일시상환 (BULLET)
        // 문제점: 이자만 계산하면 DSR이 너무 낮게 나옴.
        // 해결: 실제 만기가 1년이라도, DSR 계산 시에는 원금을 5년에 나누어 갚는 것으로 간주함.
        if (method.contains("만기") || method.contains("BULLET")) {
            // 연 이자액
            BigDecimal annualInterest = principal.multiply(rate);

            // 연 원금 상환액 (간주 만기 5년 적용)
            // 실제 만기가 5년보다 길면 실제 만기를 쓰기도 하지만, 여기서는 약식으로 5년 적용
            BigDecimal annualPrincipal = principal.divide(DEEMED_TERM_YEARS, 2, RoundingMode.HALF_UP);

            return annualPrincipal.add(annualInterest);
        }

        // 2. 원리금 균등 상환 (PMT)
        // 실제 만기 기간을 기준으로 계산
        if (method.contains("원리금") || method.contains("EQUAL_PRINCIPAL_INTEREST")) {
            // 대출 기간이 1년 미만인 경우 등에 대한 방어 로직 필요할 수 있음
            if (termMonths == 0) termMonths = 12;
            return calculatePmt(principal, rate, termMonths).multiply(new BigDecimal("12"));
        }

        // 3. 원금 균등 상환
        // (총 원금 / 년수) + (첫 달 이자 + 마지막 달 이자 / 2 * 12) ... 약식 평균
        if (method.contains("원금") || method.contains("EQUAL_PRINCIPAL")) {
            BigDecimal termYears = new BigDecimal(termMonths).divide(new BigDecimal("12"), 4, RoundingMode.HALF_UP);

            // 연 원금 상환액
            BigDecimal annualPrincipal = principal.divide(termYears, 2, RoundingMode.HALF_UP);

            // 연 평균 이자액 (잔액이 줄어들므로 전체 기간 평균으로 근사치 계산)
            // (총이자 ≈ 원금 * 연이율 * 기간(년) / 2) -> 이를 다시 연간으로 환산
            BigDecimal averageAnnualInterest = principal.multiply(rate).multiply(new BigDecimal("0.5"));

            return annualPrincipal.add(averageAnnualInterest);
        }

        // 기본값: 안전하게 만기일시(5년 분할) 로직 태움
        return principal.divide(DEEMED_TERM_YEARS, 2, RoundingMode.HALF_UP).add(principal.multiply(rate));
    }
    private BigDecimal calculatePmt(BigDecimal principal, BigDecimal annualRate, Integer termMonths) {
        // ... (기존 동일)
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        BigDecimal pow = BigDecimal.ONE.add(monthlyRate).pow(termMonths);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(pow);
        BigDecimal denominator = pow.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}