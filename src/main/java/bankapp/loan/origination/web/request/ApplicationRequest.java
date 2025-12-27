package bankapp.loan.origination.web.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ApplicationRequest {

    @NotNull
    private BigDecimal loanAmount;

    @NotNull
    private Integer loanTerm;

    @NotNull
    private String repaymentMethod;

    @NotNull
    private String interestRateType;

}

