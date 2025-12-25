package bankapp.loan.origination.component;

import bankapp.loan.common.enums.*;
import bankapp.loan.origination.web.request.CreditCheckRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class LoanInquiryScorer {

    private static final Map<TotalAssets, Integer> ASSET_SCORES = Map.of(
            TotalAssets.ASSET_A, 10,
            TotalAssets.ASSET_B, 20,
            TotalAssets.ASSET_C, 30,
            TotalAssets.ASSET_D, 40
    );

    private static final Map<TotalIncome, Integer> INCOME_SCORES = Map.of(
            TotalIncome.INCOME_A, 10,
            TotalIncome.INCOME_B, 20,
            TotalIncome.INCOME_C, 35,
            TotalIncome.INCOME_D, 50
    );

    private static final Map<TotalDebt, Integer> DEBT_SCORES = Map.of(
            TotalDebt.DEBT_A, 50,
            TotalDebt.DEBT_B, 35,
            TotalDebt.DEBT_C, 20,
            TotalDebt.DEBT_D, 5
    );

    private static final Map<FixedExpenses, Integer> EXPENSE_SCORES = Map.of(
            FixedExpenses.EXPENSE_A, 40,
            FixedExpenses.EXPENSE_B, 30,
            FixedExpenses.EXPENSE_C, 15,
            FixedExpenses.EXPENSE_D, 5
    );

    /**
     * 사용자의 입력(Request)을 받아 신용 가산 금리 계산
     */
    public BigDecimal getCreditSpread(CreditCheckRequest request) {

        int totalScore = 0;
        totalScore += ASSET_SCORES.getOrDefault(request.getTotalAssets(), 0);
        totalScore += INCOME_SCORES.getOrDefault(request.getTotalIncome(), 0);
        totalScore += DEBT_SCORES.getOrDefault(request.getTotalDebt(), 0);
        totalScore += EXPENSE_SCORES.getOrDefault(request.getFixedExpenses(), 0);

        return mapGradeToCreditSpread(mapScoreToGrade(totalScore));
    }

    /**
     * 총점을 기준으로 FinancialGrade를 결정
     */
    private FinancialGrade mapScoreToGrade(int score) {
        // 예시 점수 범위 (총점 180점 만점 기준)
        if (score >= 150) {
            return FinancialGrade.SECURE;
        } else if (score >= 120) {
            return FinancialGrade.STABLE;
        } else if (score >= 90) {
            return FinancialGrade.STANDARD;
        } else if (score >= 60) {
            return FinancialGrade.CAUTION;
        } else {
            return FinancialGrade.RISK;
        }
    }

    /**
     * FinancialGrade를 기준으로 신용 가산 금리 결정
     */
    private BigDecimal mapGradeToCreditSpread(FinancialGrade financialGrade){

        return switch (financialGrade) {
            case SECURE -> new BigDecimal("1.0");   // 최우수
            case STABLE -> new BigDecimal("2.0");   // 우수
            case STANDARD -> new BigDecimal("3.0"); // 보통
            case CAUTION -> new BigDecimal("4.0");  // 주의
            case RISK -> new BigDecimal("5.0");     // 위험
        };
    }



}
