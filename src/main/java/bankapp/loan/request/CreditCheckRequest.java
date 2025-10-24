package bankapp.loan.request;

import bankapp.loan.common.enums.FixedExpenses;
import bankapp.loan.common.enums.TotalAssets;
import bankapp.loan.common.enums.TotalDebt;
import bankapp.loan.common.enums.TotalIncome;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreditCheckRequest {
    @NotNull // '선택하세요'를 선택하지 않았는지 검증
    private TotalAssets totalAssets;

    @NotNull
    private TotalIncome totalIncome;

    @NotNull
    private TotalDebt totalDebt;

    @NotNull
    private FixedExpenses fixedExpenses;

}
