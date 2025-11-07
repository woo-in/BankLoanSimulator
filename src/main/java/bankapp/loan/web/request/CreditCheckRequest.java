package bankapp.loan.web.request;

import bankapp.loan.common.enums.FixedExpenses;
import bankapp.loan.common.enums.TotalAssets;
import bankapp.loan.common.enums.TotalDebt;
import bankapp.loan.common.enums.TotalIncome;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreditCheckRequest {

    @NotNull
    private TotalAssets totalAssets;

    @NotNull
    private TotalIncome totalIncome;

    @NotNull
    private TotalDebt totalDebt;

    @NotNull
    private FixedExpenses fixedExpenses;

}
