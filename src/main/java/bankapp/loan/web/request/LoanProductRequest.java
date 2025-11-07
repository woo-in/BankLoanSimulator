package bankapp.loan.web.request;

import bankapp.loan.model.common.product.LoanProduct;
import bankapp.loan.model.common.product.ProductStatus;
import bankapp.loan.model.credit.CreditLoanProduct;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class LoanProductRequest {

    private String loanProductName;
    private String loanProductSlug;
    private String loanProductDescription;
    private String loanType;
    private BigDecimal maxLoanAmount;
    private BigDecimal defaultSpread;
    private Integer maxLoanTerm;
    private ProductStatus status;


    private List<Long> interestRateTypeIds = new ArrayList<>();
    private List<Long> repaymentMethodIds = new ArrayList<>();


    public LoanProduct toEntity(){

        // TODO: 일단은 신용대출을 선택했다고 가정 , 다른 대출도 추가
        return CreditLoanProduct.builder()
                .loanProductName(loanProductName)
                .loanProductSlug(loanProductSlug)
                .loanProductDescription(loanProductDescription)
                .loanType(loanType)
                .maxLoanAmount(maxLoanAmount)
                .maxLoanTerm(maxLoanTerm)
                .defaultSpread(defaultSpread)
                .status(status)
                .build();
    }




    // TODO: 여기에 각 필드에 대한 유효성 검사(Validation) 어노테이션 (예: @NotBlank, @NotNull, @Size, @Min)을 추가하는 것을 권장합니다.
    // TODO: 일단은 신용대출을 선택했다고 가정 , 다른 대출도 추가
}
