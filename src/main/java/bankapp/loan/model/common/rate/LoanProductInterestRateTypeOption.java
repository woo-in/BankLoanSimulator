package bankapp.loan.model.common.rate;

import bankapp.loan.model.common.product.LoanProduct;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class LoanProductInterestRateTypeOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "product_id" , nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "interest_rate_type_id" , nullable = false)
    private InterestRateType interestRateType;


    // 연관관계 편의 메서드
    public void setInterestRateType(InterestRateType interestRateType) {

        if(this.interestRateType != null){
            this.interestRateType.getInterestRateTypeOptions().remove(this);
        }
        this.interestRateType = interestRateType;
        this.interestRateType.getInterestRateTypeOptions().add(this);
    }

    public void setLoanProduct(LoanProduct loanProduct) {

        if(this.loanProduct != null){
            this.loanProduct.getInterestRateTypeOptions().remove(this);
        }
        this.loanProduct = loanProduct;
        loanProduct.getInterestRateTypeOptions().add(this);

    }

}
