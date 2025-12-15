package bankapp.loan.product.model;

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

    // 연관관계 편의 메서드
    public void setRepaymentMethod(RepaymentMethod repaymentMethod) {

        if(this.repaymentMethod != null) {
            this.repaymentMethod.getLoanProductRepaymentOptions().remove(this);
        }
        this.repaymentMethod = repaymentMethod;
        this.repaymentMethod.getLoanProductRepaymentOptions().add(this);
    }


    public void setLoanProduct(LoanProduct loanProduct) {
        if(this.loanProduct != null) {
            this.loanProduct.getRepaymentOptions().remove(this);
        }
        this.loanProduct = loanProduct;
        this.loanProduct.getRepaymentOptions().add(this);
    }


//    public LoanProductRepaymentOption(LoanProduct loanProduct, RepaymentMethod repaymentMethod) {
//        this.loanProduct = loanProduct;
//        this.repaymentMethod = repaymentMethod;
//    }
}
