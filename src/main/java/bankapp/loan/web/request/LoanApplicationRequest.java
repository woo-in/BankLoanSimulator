package bankapp.loan.web.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class LoanApplicationRequest {

    @NotNull
    private BigDecimal loanAmount;

    @NotNull
    private Integer loanTerm;

    @NotNull
    private Long repaymentMethodId;

    @NotNull
    private Long interestRateTypeId;
}

