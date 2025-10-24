package bankapp.loan.model.product.repayment;

import bankapp.loan.model.product.LoanProduct;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class LoanProductRepaymentOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "product_id" , nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "repayment_method_id" , nullable = false)
    private RepaymentMethod repaymentMethod;


    public LoanProductRepaymentOption(LoanProduct loanProduct, RepaymentMethod repaymentMethod) {
        this.loanProduct = loanProduct;
        this.repaymentMethod = repaymentMethod;
    }
}
