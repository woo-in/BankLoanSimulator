package bankapp.loan.underwriting.web.request;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@Data
public class ApprovedLoanApplicationDto {

    // review
    private BigDecimal approvedLoanAmount;
    private Integer approvedLoanTerm;
    private BigDecimal approvedBaseRate;
    private BigDecimal approvedProductSpread;
    private BigDecimal approvedCreditSpread;
    private BigDecimal approvedSelectionSpread;

    // complete
    private BigDecimal approvedFinalInterestRate;
    private BigDecimal calculatedDsr;
    private String messageToCustomer;
}
