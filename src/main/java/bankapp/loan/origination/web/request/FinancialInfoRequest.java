package bankapp.loan.origination.web.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class FinancialInfoRequest {

    @NotNull(message = "총 자산 규모는 필수 입력값입니다.")
    @PositiveOrZero(message = "자산은 0원 이상이어야 합니다.")
    private BigDecimal totalAssetsAmount;

    @NotNull(message = "연 소득은 필수 입력값입니다.")
    @PositiveOrZero(message = "연 소득은 0원 이상이어야 합니다.")
    private BigDecimal annualIncomeAmount;

    @NotNull(message = "고정 지출은 필수 입력값입니다.")
    @PositiveOrZero(message = "고정 지출은 0원 이상이어야 합니다.")
    private BigDecimal fixedExpensesAmount;

    private String externalLoansJson;
}