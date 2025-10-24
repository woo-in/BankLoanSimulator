package bankapp.loan.model.product.rate;

import bankapp.loan.model.product.LoanProduct;
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

}
