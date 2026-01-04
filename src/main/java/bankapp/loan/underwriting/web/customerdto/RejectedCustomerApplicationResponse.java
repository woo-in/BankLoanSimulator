package bankapp.loan.underwriting.web.customerdto;

import bankapp.loan.underwriting.model.ApplicationStatus;
import bankapp.loan.underwriting.model.LoanApplication;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Data
public class RejectedCustomerApplicationResponse {

    private Long loanApplicationId;
    private String loanProductName;
    private String loanProductType;

    private String repaymentMethodName;
    private String interestRateTypeName;

    private BigDecimal appliedLoanAmount;
    private Integer appliedLoanTerm;
    private BigDecimal appliedFinalInterestRate;

    private ApplicationStatus applicationStatus;
    private String messageToCustomer;

    private LocalDateTime createdAt;


    public static RejectedCustomerApplicationResponse from(LoanApplication app) {
        RejectedCustomerApplicationResponse response = new RejectedCustomerApplicationResponse();

        response.setLoanApplicationId(app.getLoanApplicationId());
        response.setLoanProductName(app.getLoanProduct().getLoanProductName());
        response.setLoanProductType(app.getLoanProduct().getLoanType());

        response.setRepaymentMethodName(app.getRepaymentMethod().getMethodName());
        response.setInterestRateTypeName(app.getInterestRateType().getTypeName());


        response.setAppliedLoanAmount(app.getLoanAmount());
        response.setAppliedLoanTerm(app.getLoanTerm());
        response.setAppliedFinalInterestRate(app.getFinalInterestRate());

        response.setApplicationStatus(app.getApplicationStatus());
        response.setMessageToCustomer(app.getMessageToCustomer());

        response.setCreatedAt(app.getCreatedAt());

        return response;

    }


}
