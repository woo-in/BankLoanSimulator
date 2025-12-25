package bankapp.loan.origination.web.response;

import bankapp.loan.origination.model.LoanContract;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExistingLoanResponse {

    private String loanProductName;
    private String loanType;
    private BigDecimal loanAmount;
    private Integer loanTerm;
    private String repaymentMethodName;
    private BigDecimal totalInterestRate;
    private boolean isExternal;

    /**
     * Entity -> DTO 변환 팩토리 메서드
     */
    public static ExistingLoanResponse from(LoanContract contract) {

        BigDecimal totalRate = contract.getContractBaseRate()
                .add(contract.getContractProductSpread())
                .add(contract.getContractCreditSpread());

        return ExistingLoanResponse.builder()
                .loanProductName(contract.getLoanProduct().getLoanProductName())
                .loanType(contract.getLoanProduct().getLoanType())
                .loanAmount(contract.getLoanAmount())
                .loanTerm(contract.getLoanTerm())
                .repaymentMethodName(contract.getRepaymentMethod().getMethodName())
                .totalInterestRate(totalRate)
                .build();
    }
}
