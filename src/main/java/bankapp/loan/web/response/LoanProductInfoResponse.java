//package bankapp.loan.web.response;
//
//import bankapp.loan.model.common.product.LoanProduct;
//import lombok.Data;
//
//import java.math.BigDecimal;
//
//@Data
//public class LoanProductInfoResponse {
//
//    private String loanProductName;
//    private String loanProductSlug;
//    private String productDescription;
//
//    private BigDecimal maxInterestRate;
//    private BigDecimal minInterestRate;
//
//    private BigDecimal maxLoanAmount;
//    private Integer maxLoanTerm;
//    private String repaymentMethods;
//
//    private LoanProductInfoResponse() { }
//
//    public static LoanProductInfoResponse from(LoanProduct loanProduct){
//        LoanProductInfoResponse loanProductInfoResponse = new LoanProductInfoResponse();
//        loanProductInfoResponse.setLoanProductName(loanProduct.getLoanProductName());
//        loanProductInfoResponse.setProductDescription(loanProduct.getLoanProductDetail().getProductDescription());
//        loanProductInfoResponse.setMaxInterestRate(loanProduct.getMaxInterestRate());
//        loanProductInfoResponse.setMinInterestRate(loanProduct.getMinInterestRate());
//        loanProductInfoResponse.setMaxLoanAmount(loanProduct.getMaxLoanAmount());
//        loanProductInfoResponse.setMaxLoanTerm(loanProduct.getMaxLoanTerm());
//        loanProductInfoResponse.setLoanProductSlug(loanProduct.getLoanProductSlug());
//
//        StringBuilder methodsBuilder = new StringBuilder();
//        for (RepaymentMethod method : loanProduct.getRepaymentMethods()) {
//            if (!methodsBuilder.isEmpty()) {
//                methodsBuilder.append(", ");
//            }
//            methodsBuilder.append(method.getDisplayName());
//        }
//        loanProductInfoResponse.setRepaymentMethods(methodsBuilder.toString());
//
//        return loanProductInfoResponse;
//    }
//
//}
//
//
