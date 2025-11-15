package bankapp.loan.model.common.application;

import bankapp.loan.model.common.product.LoanProduct;
import bankapp.loan.model.common.rate.InterestRateType;
import bankapp.loan.model.common.repayment.RepaymentMethod;
import bankapp.member.model.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanApplicationId;

    // todo : onetomany
    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "member_id" , nullable = false)
    private Member member;

    // todo : onetomany
    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "loan_product_id" , nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "repayment_method_id" , nullable = false)
    private RepaymentMethod repaymentMethod;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "interest_rate_type_id" , nullable = false)
    private InterestRateType interestRateType;

    @Column(nullable = false)
    private BigDecimal loanAmount;

    @Column(nullable = false)
    private Integer loanTerm;

    @Column(nullable = false)
    private BigDecimal appliedBaseRate;

    @Column(nullable = false)
    private BigDecimal appliedProductSpread;

    @Column(nullable = false)
    private BigDecimal appliedCreditSpread;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus applicationStatus;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;


}
