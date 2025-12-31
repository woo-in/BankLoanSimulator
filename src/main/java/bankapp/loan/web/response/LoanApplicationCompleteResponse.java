package bankapp.loan.web.response;

import bankapp.loan.underwriting.model.LoanApplication;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class LoanApplicationCompleteResponse {


    private Long loanApplicationId; // 신청 식별자

    private Long memberId; // 유저 식별자
    private String name; // 유저 이름

    private Long loanProductId;  // 상품 식별자
    private String loanProductName; // 상품명
    private String loanType ; // 대출 상품 종류

    private String repaymentMethodName;  // 상환 방법 (이름)
    private String interestRateTypeName; // 금리 종류 (이름)
    private BigDecimal loanAmount;       // 대출 금액
    private Integer loanTerm;            // 대출 기간

    private BigDecimal appliedBaseRate; // 신청당시 기준금리
    private BigDecimal appliedProductSpread; // 신청 당시 상품 가산 금리
    private BigDecimal appliedCreditSpread; // 신청 당시 신용 가산 금리


    private LocalDateTime createdAt; // 최초 신청일시



    public static LoanApplicationCompleteResponse from(LoanApplication application) {
        LoanApplicationCompleteResponse response = new LoanApplicationCompleteResponse();

        response.setLoanApplicationId(application.getLoanApplicationId());

        if(application.getMember() != null){
            response.setMemberId(application.getMember().getMemberId());
            response.setName(application.getMember().getName());
        }

        if (application.getLoanProduct() != null) {
            response.setLoanProductId(application.getLoanProduct().getLoanProductId());
            response.setLoanProductName(application.getLoanProduct().getLoanProductName());
            response.setLoanType(application.getLoanProduct().getLoanType());
        }

        response.setLoanAmount(application.getLoanAmount());
        response.setLoanTerm(application.getLoanTerm());
        response.setAppliedBaseRate(application.getBaseRate());
        response.setAppliedProductSpread(application.getProductSpread());
        response.setAppliedCreditSpread(application.getCreditSpread());

        if (application.getRepaymentMethod() != null) {
            response.setRepaymentMethodName(application.getRepaymentMethod().getMethodName());
        }

        if (application.getInterestRateType() != null) {
            response.setInterestRateTypeName(application.getInterestRateType().getTypeName());
        }

        response.setCreatedAt(application.getCreatedAt());

        return response;
    }
}