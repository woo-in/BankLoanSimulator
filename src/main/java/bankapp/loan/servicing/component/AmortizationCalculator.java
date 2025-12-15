package bankapp.loan.servicing.component;

import bankapp.loan.origination.model.LoanContract;
import bankapp.loan.servicing.model.RepaymentDetail;
import bankapp.loan.product.model.RepaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class AmortizationCalculator {

    // 금융 계산을 위한 정밀도 설정 (소수점 10자리까지, 반올림)
    private static final int CALCULATION_SCALE = 10;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final MathContext MC = new MathContext(CALCULATION_SCALE, ROUNDING_MODE);

    /**
     * 대출 계약 정보를 바탕로 상환 스케줄(원금, 이자) 리스트를 계산합니다.
     * @param loanContract 대출 계약서
     * @return List<RepaymentDetail> 회차별 상환 상세 정보
     */
    public List<RepaymentDetail> calculate(LoanContract loanContract) {

        // 1. 최종 적용 금리 (연간) 계산
        // (interestRateType이 '고정금리'라고 가정)
        BigDecimal totalAppliedRate = loanContract.getContractBaseRate()
                .add(loanContract.getContractProductSpread())
                .add(loanContract.getContractCreditSpread());

        // 연이율을 백분율에서 소수점으로 변환
        BigDecimal decimalRate = totalAppliedRate.divide(BigDecimal.valueOf(100), MC);

        // 월 이율 계산 (소수점으로 변환한 연이율 / 12)
        BigDecimal monthlyRate = decimalRate.divide(BigDecimal.valueOf(12), MC);

        BigDecimal principal = loanContract.getLoanAmount(); // 총 대출 원금
        int term = loanContract.getLoanTerm(); // 기간 (개월)

        // 3. 상환 방식에 따라 다른 계산 전략 선택
        RepaymentMethod method = loanContract.getRepaymentMethod();

        // todo : 원리금균등상환 , 원금균등상환 , 원금만기일시상환 으로 은행원 툴에서 등록했다고 가정
        if ("원리금균등상환".equals(method.getMethodName())) {
            return calculateEqualInstallment(principal, monthlyRate, term);
        } else if ("원금균등상환".equals(method.getMethodName())) {
            return calculateEqualPrincipal(principal, monthlyRate, term);
        } else if ("원금만기일시상환".equals(method.getMethodName())) {
            return calculateBulletPayment(principal, monthlyRate, term);
        } else {
            throw new IllegalArgumentException("지원하지 않는 상환 방식입니다: " + method.getMethodName());
        }
    }

    /**
     * 1. 원리금균등상환 (Equal Installment)
     * M = P * (r * (1+r)^n) / ((1+r)^n - 1)
     */
    private List<RepaymentDetail> calculateEqualInstallment(BigDecimal principal, BigDecimal monthlyRate, int term) {
        List<RepaymentDetail> details = new ArrayList<>();
        BigDecimal remainingPrincipal = principal;

        // 월 상환금(M) 계산
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = onePlusRate.pow(term, MC);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);

        // 월 상환금 (소수점 0자리, 반올림)
        BigDecimal monthlyPayment = numerator.divide(denominator, 0, ROUNDING_MODE);

        for (int i = 1; i <= term; i++) {
            // 이자 = 남은원금 * 월이율
            BigDecimal interest = remainingPrincipal.multiply(monthlyRate).setScale(0, ROUNDING_MODE);

            // 원금 = 월상환금 - 이자
            BigDecimal principalPayment = monthlyPayment.subtract(interest);

            // 마지막 회차 보정 (잔액 0 맞추기)
            if (i == term) {
                principalPayment = remainingPrincipal;
                interest = monthlyPayment.subtract(principalPayment); // 역계산
            }

            remainingPrincipal = remainingPrincipal.subtract(principalPayment);
            details.add(new RepaymentDetail(i, principalPayment, interest));
        }
        return details;
    }

    /**
     * 2. 원금균등상환 (Equal Principal)
     * 매월 동일한 원금 상환 + 남은 원금에 대한 이자
     */
    private List<RepaymentDetail> calculateEqualPrincipal(BigDecimal principal, BigDecimal monthlyRate, int term) {
        List<RepaymentDetail> details = new ArrayList<>();

        // 매월 상환 원금 (고정)
        BigDecimal principalPayment = principal.divide(BigDecimal.valueOf(term), 0, ROUNDING_MODE);
        BigDecimal remainingPrincipal = principal;

        for (int i = 1; i <= term; i++) {
            // 이자 = 남은원금 * 월이율
            BigDecimal interest = remainingPrincipal.multiply(monthlyRate).setScale(0, ROUNDING_MODE);

            // 마지막 회차 보정
            if (i == term) {
                principalPayment = remainingPrincipal;
            }

            remainingPrincipal = remainingPrincipal.subtract(principalPayment);
            details.add(new RepaymentDetail(i, principalPayment, interest));
        }
        return details;
    }

    /**
     * 3. 원금만기일시상환 (Bullet Payment)
     * 매월 이자만 내고, 마지막에 원금 전부 상환
     */
    private List<RepaymentDetail> calculateBulletPayment(BigDecimal principal, BigDecimal monthlyRate, int term) {
        List<RepaymentDetail> details = new ArrayList<>();

        // 매월 고정 이자
        BigDecimal interest = principal.multiply(monthlyRate).setScale(0, ROUNDING_MODE);

        for (int i = 1; i <= term; i++) {
            if (i < term) {
                // (n-1) 회차까지: 이자만, 원금 0
                details.add(new RepaymentDetail(i, BigDecimal.ZERO, interest));
            } else {
                // 마지막 회차 (n): 원금 전부 + 마지막 이자
                details.add(new RepaymentDetail(i, principal, interest));
            }
        }
        return details;
    }

}
