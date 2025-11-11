package bankapp.loan.web.response;

import bankapp.loan.model.common.application.LoanApplication;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class LoanApplicationCompleteResponse {

    private String loanProductName;      // 상품명
    private String repaymentMethodName;  // 상환 방법 (이름)
    private String interestRateTypeName; // 금리 종류 (이름)
    private BigDecimal loanAmount;       // 대출 금액
    private Integer loanTerm;            // 대출 기간
    private BigDecimal finalInterestRate; // 최종 금리 (Entity의 appliedRate)

    public static LoanApplicationCompleteResponse from(LoanApplication application) {
        LoanApplicationCompleteResponse response = new LoanApplicationCompleteResponse();


        if (application.getLoanProduct() != null) {
            response.setLoanProductName(application.getLoanProduct().getLoanProductName());
        }

        response.setLoanAmount(application.getLoanAmount());
        response.setLoanTerm(application.getLoanTerm());
        response.setFinalInterestRate(application.getAppliedRate());

        if (application.getRepaymentMethod() != null) {
            response.setRepaymentMethodName(application.getRepaymentMethod().getMethodName());
        }

        if (application.getInterestRateType() != null) {
            response.setInterestRateTypeName(application.getInterestRateType().getTypeName());
        }

        return response;
    }
}