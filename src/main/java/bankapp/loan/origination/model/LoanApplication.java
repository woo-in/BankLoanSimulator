package bankapp.loan.origination.model;

import bankapp.loan.product.model.LoanProduct;
import bankapp.loan.product.model.InterestRateType;
import bankapp.loan.product.model.RepaymentMethod;
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

    // todo : 리스크 조정 금리를 왜 미리 계산 안하는가 ?

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus applicationStatus;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static LoanApplication createFrom(PendingLoanApplication pending,
                                             BigDecimal appliedBaseRate,
                                             BigDecimal appliedProductSpread,
                                             BigDecimal appliedCreditSpread) {

        LoanApplication app = new LoanApplication();

        app.setMember(pending.getMember());
        app.setLoanProduct(pending.getLoanProduct());

        app.setLoanAmount(pending.getRequestLoanAmount());
        app.setLoanTerm(pending.getRequestLoanTerm());
        app.setRepaymentMethod(pending.getRepaymentMethod());
        app.setInterestRateType(pending.getInterestRateType());

        app.setAppliedBaseRate(appliedBaseRate);
        app.setAppliedProductSpread(appliedProductSpread);
        app.setAppliedCreditSpread(appliedCreditSpread);

        app.setApplicationStatus(ApplicationStatus.APPLIED);

        return app;
    }


}
