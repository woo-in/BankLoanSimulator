package bankapp.loan.servicing.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class RepaymentAllocationInfo {

    private final LocalDateTime transactionDate;
    private final BigDecimal totalRepaymentAmount;
    private final BigDecimal principalAmount;
    private final BigDecimal interestAmount;
    private final BigDecimal delinquentAmount;
    private final BigDecimal accelerationPenaltyAmount;
    private final BigDecimal loanBalanceAfterTransaction;

}
