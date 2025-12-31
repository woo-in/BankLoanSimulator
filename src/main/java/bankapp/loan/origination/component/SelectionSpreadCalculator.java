package bankapp.loan.origination.component;

import bankapp.loan.origination.model.PendingLoanApplication;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class SelectionSpreadCalculator {

    private static final BigDecimal BASE_SPREAD = BigDecimal.ZERO;
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("50000000"); // 5천만원
    private static final BigDecimal HIGH_AMOUNT_SPREAD = new BigDecimal("0.10"); // 5천만원 이상 시 +0.1%

    private static final Integer LONG_TERM_THRESHOLD = 36; // 36개월
    private static final BigDecimal LONG_TERM_SPREAD = new BigDecimal("0.10"); // 36개월 이상 시 +0.1%

    private static final BigDecimal REPAYMENT_BULLET_SPREAD = new BigDecimal("0.20"); // 만기일시상환 시 +0.2%
    private static final BigDecimal RATE_TYPE_FIXED_SPREAD = new BigDecimal("0.30"); // 고정금리 시 +0.3%

    public BigDecimal calculate(PendingLoanApplication pendingLoanApplication) {
        BigDecimal totalSpread = BASE_SPREAD;

        // 1. 대출 금액에 따른 가산 (고액 대출 리스크)
        if (pendingLoanApplication.getRequestLoanAmount().compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {
            totalSpread = totalSpread.add(HIGH_AMOUNT_SPREAD);
        }

        // 2. 대출 기간에 따른 가산 (장기 대출 리스크)
        if (pendingLoanApplication.getRequestLoanTerm() >= LONG_TERM_THRESHOLD) {
            totalSpread = totalSpread.add(LONG_TERM_SPREAD);
        }

//        // 3. 상환 방법에 따른 가산 (만기일시상환이 리스크가 더 큼)
//        if (pendingLoanApplication.getRepaymentMethod().getMethodName().equals("원금만기일시상환") ||
//        pendingLoanApplication.getRepaymentMethod().getMethodCode().equals("BULLET")) {
//            totalSpread = totalSpread.add(REPAYMENT_BULLET_SPREAD);
//        }
//
//
//        // 4. 금리 종류에 따른 가산 (고정금리는 은행의 리스크 헤지 비용 발생)
//        if (pendingLoanApplication.getInterestRateType().getTypeName().equals("고정금리") ||
//        pendingLoanApplication.getInterestRateType().getTypeCode().equals("FIXED")) {
//            totalSpread = totalSpread.add(RATE_TYPE_FIXED_SPREAD);
//        }

        return totalSpread;
    }
}