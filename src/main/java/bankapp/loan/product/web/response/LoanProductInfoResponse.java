package bankapp.loan.product.web.response;

import bankapp.loan.product.model.LoanProduct;
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
    private BigDecimal minLoanAmount;
    private BigDecimal applicationAmountUnit;

    private Integer maxLoanTerm;
    private Integer minLoanTerm;
    private Integer applicationTermUnit;

    private BigDecimal defaultSpread;
    private String status;

    private String interestRateTypeOptions;
    private String repaymentOptions;

    private LoanProductInfoResponse() { }

    public static LoanProductInfoResponse from(LoanProduct loanProduct){
        LoanProductInfoResponse loanProductInfoResponse = new LoanProductInfoResponse();

        loanProductInfoResponse.setLoanProductName(loanProduct.getLoanProductName());
        loanProductInfoResponse.setLoanProductSlug(loanProduct.getLoanProductSlug());
        loanProductInfoResponse.setProductDescription(loanProduct.getLoanProductDescription());

        loanProductInfoResponse.setLoanType(loanProduct.getLoanType());

        loanProductInfoResponse.setMaxLoanAmount(loanProduct.getMaxLoanAmount());
        loanProductInfoResponse.setMinLoanAmount(loanProduct.getMinLoanAmount());
        loanProductInfoResponse.setApplicationAmountUnit(loanProduct.getApplicationAmountUnit());

        loanProductInfoResponse.setMaxLoanTerm(loanProduct.getMaxLoanTerm());
        loanProductInfoResponse.setMinLoanTerm(loanProduct.getMinLoanTerm());
        loanProductInfoResponse.setApplicationTermUnit(loanProduct.getApplicationTermUnit());

        loanProductInfoResponse.setDefaultSpread(loanProduct.getDefaultSpread());
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


