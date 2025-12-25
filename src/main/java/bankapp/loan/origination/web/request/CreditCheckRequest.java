package bankapp.loan.origination.web.request;

import bankapp.loan.common.enums.FixedExpenses;
import bankapp.loan.common.enums.TotalAssets;
import bankapp.loan.common.enums.TotalDebt;
import bankapp.loan.common.enums.TotalIncome;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
public class CreditCheckRequest {

    private TotalAssets totalAssets;

    private TotalIncome totalIncome;

    private TotalDebt totalDebt;

    private FixedExpenses fixedExpenses;

    /**
     * 사용자의 정확한 재무 정보와 계산된 총 부채를 바탕으로
     * 신용평가용 Request 객체(Enum 기반)를 생성합니다.
     *
     * @param userInfo 사용자 입력 정보 (자산, 소득, 지출)
     * @param calculatedTotalDebt 시스템이 합산한 총 부채 금액 (내부 + 외부)
     * @return 생성된 CreditCheckRequest
     */
    public static CreditCheckRequest from(UserFinancialInfoRequest userInfo, BigDecimal calculatedTotalDebt) {
        CreditCheckRequest request = new CreditCheckRequest();

        request.totalAssets = mapToAssetsEnum(userInfo.getTotalAssetsAmount());
        request.totalIncome = mapToIncomeEnum(userInfo.getAnnualIncomeAmount());
        request.fixedExpenses = mapToExpensesEnum(userInfo.getFixedExpensesAmount(), userInfo.getAnnualIncomeAmount());
        request.totalDebt = mapToDebtEnum(calculatedTotalDebt);

        return request;
    }

    private static TotalAssets mapToAssetsEnum(BigDecimal amount) {
        long val = amount.longValue();
        if (val < 100_000_000) return TotalAssets.ASSET_A; // 1억 미만
        if (val < 500_000_000) return TotalAssets.ASSET_B; // 1억 ~ 5억
        if (val < 1_000_000_000) return TotalAssets.ASSET_C; // 5억 ~ 10억
        return TotalAssets.ASSET_D; // 10억 이상
    }

    private static TotalIncome mapToIncomeEnum(BigDecimal amount) {
        long val = amount.longValue();
        if (val < 25_000_000) return TotalIncome.INCOME_A; // 2.5천 미만
        if (val < 50_000_000) return TotalIncome.INCOME_B; // 2.5천 ~ 5천
        if (val < 100_000_000) return TotalIncome.INCOME_C; // 5천 ~ 1억
        return TotalIncome.INCOME_D; // 1억 이상
    }

    private static FixedExpenses mapToExpensesEnum(BigDecimal expenses, BigDecimal income) {
        if (income.compareTo(BigDecimal.ZERO) == 0) return FixedExpenses.EXPENSE_D;

        // 비율 계산: (지출 / 소득) * 100
        double ratio = expenses.divide(income, 4, RoundingMode.HALF_UP).doubleValue() * 100;

        if (ratio < 30) return FixedExpenses.EXPENSE_A; // 30% 미만
        if (ratio < 50) return FixedExpenses.EXPENSE_B; // 30% ~ 50%
        if (ratio < 70) return FixedExpenses.EXPENSE_C; // 50% ~ 70%
        return FixedExpenses.EXPENSE_D; // 70% 이상
    }

    private static TotalDebt mapToDebtEnum(BigDecimal amount) {
        if (amount == null) return TotalDebt.DEBT_A;
        long val = amount.longValue();

        if (val < 10_000_000) return TotalDebt.DEBT_A; // 1천만원 미만
        if (val < 50_000_000) return TotalDebt.DEBT_B; // 1천 ~ 5천
        if (val < 100_000_000) return TotalDebt.DEBT_C; // 5천 ~ 1억
        return TotalDebt.DEBT_D; // 1억 이상
    }
}