package bankapp.loan.underwriting.web.customerdto;

import bankapp.loan.underwriting.model.ApplicationStatus;
import bankapp.loan.underwriting.model.LoanApplication;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Data
public class ApprovedCustomerApplicationResponse {

    // todo : 고객 , 은행원 보여지는 DTO 구분

    private Long loanApplicationId;
    private String loanProductName;
    private String loanProductType;

    private String repaymentMethodName;
    private String interestRateTypeName;

    private BigDecimal appliedLoanAmount;
    private Integer appliedLoanTerm;
    private BigDecimal appliedFinalInterestRate;

    private BigDecimal approvedLoanAmount;
    private Integer approvedLoanTerm;
    private BigDecimal approvedFinalInterestRate;

    private String messageToCustomer;

    private ApplicationStatus applicationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ApprovedCustomerApplicationResponse from(LoanApplication app) {
        ApprovedCustomerApplicationResponse response = new ApprovedCustomerApplicationResponse();


        response.setLoanApplicationId(app.getLoanApplicationId());
        response.setLoanProductName(app.getLoanProduct().getLoanProductName());
        response.setLoanProductType(app.getLoanProduct().getLoanType());
        response.setRepaymentMethodName(app.getRepaymentMethod().getMethodName());
        response.setInterestRateTypeName(app.getInterestRateType().getTypeName());


        response.setAppliedLoanAmount(app.getLoanAmount());
        response.setAppliedLoanTerm(app.getLoanTerm());
        response.setAppliedFinalInterestRate(app.getFinalInterestRate());

        response.setApprovedLoanAmount(app.getApprovedLoanAmount());
        response.setApprovedLoanTerm(app.getApprovedLoanTerm());
        response.setApprovedFinalInterestRate(app.getApprovedFinalInterestRate());

        response.setMessageToCustomer(app.getMessageToCustomer());

        response.setApplicationStatus(app.getApplicationStatus());
        response.setCreatedAt(app.getCreatedAt());
        response.setUpdatedAt(app.getUpdatedAt());

        return response;

    }


}
