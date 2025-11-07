package bankapp.loan.web.response;

import bankapp.loan.model.common.product.LoanProduct;
import lombok.Data;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Data
public class LoanProductInfoResponse {

    private String loanProductName;
    private String loanProductSlug;
    private String productDescription;

    private String loanType;

    private BigDecimal maxLoanAmount;
    private BigDecimal defaultSpread;
    private Integer maxLoanTerm;
    private String status;

    private String interestRateTypeOptions;
    private String repaymentOptions;

    private LoanProductInfoResponse() { }

    public static LoanProductInfoResponse from(LoanProduct loanProduct){
        LoanProductInfoResponse loanProductInfoResponse = new LoanProductInfoResponse();

        loanProductInfoResponse.setLoanProductName(loanProduct.getLoanProductName());
        loanProductInfoResponse.setLoanProductSlug(loanProduct.getLoanProductSlug());
        loanProductInfoResponse.setProductDescription(loanProduct.getLoanProductDescription());


        loanProductInfoResponse.setMaxLoanAmount(loanProduct.getMaxLoanAmount());
        loanProductInfoResponse.setDefaultSpread(loanProduct.getDefaultSpread());
        loanProductInfoResponse.setMaxLoanTerm(loanProduct.getMaxLoanTerm());

        loanProductInfoResponse.setLoanType(loanProduct.getLoanType());
        loanProductInfoResponse.setStatus(loanProduct.getStatus().toString());

        String interestRates = loanProduct.getInterestRateTypeOptions().stream()
                .map(option -> option.getInterestRateType().getTypeName())
                .collect(Collectors.joining(", "));

        String repayments = loanProduct.getRepaymentOptions().stream()

                .map(option -> option.getRepaymentMethod().getMethodName())
                .collect(Collectors.joining(", "));

        loanProductInfoResponse.setInterestRateTypeOptions(interestRates);
        loanProductInfoResponse.setRepaymentOptions(repayments);

        return loanProductInfoResponse;
    }

}


