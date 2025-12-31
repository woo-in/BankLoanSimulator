package bankapp.loan.underwriting.model;

import bankapp.loan.origination.model.PendingLoanApplication;
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

    // 기본 정보 : id , 회원 , 상품
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


    // 대출 신청 정보 : 상환방법 , 금리종류 , 대출금 , 대출기간
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

    // 금리 정보 : 기본금리 , 상품가산금리 , 신용가산금리 , 리스크 금리 , 최종금리 , Dsr
    @Column(nullable = false)
    private BigDecimal baseRate;
    @Column(nullable = false)
    private BigDecimal productSpread;
    @Column(nullable = false)
    private BigDecimal creditSpread;
    @Column(nullable = false)
    private BigDecimal selectionSpread;
    @Column(nullable = false)
    private BigDecimal finalInterestRate;
    @Column(nullable = false)
    private BigDecimal debtServiceRatio;

    // 대출신청 상태 정보
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus applicationStatus;

    // 거절 사유
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;


    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static LoanApplication createFrom(PendingLoanApplication pending) {

        LoanApplication app = new LoanApplication();

        app.setMember(pending.getMember());
        app.setLoanProduct(pending.getLoanProduct());

        app.setLoanAmount(pending.getRequestLoanAmount());
        app.setLoanTerm(pending.getRequestLoanTerm());
        app.setRepaymentMethod(pending.getRepaymentMethod());
        app.setInterestRateType(pending.getInterestRateType());

        app.setBaseRate(pending.getBaseRate());
        app.setProductSpread(pending.getProductSpread());
        app.setCreditSpread(pending.getCreditSpread());
        app.setSelectionSpread(pending.getSelectionSpread());
        app.setFinalInterestRate(pending.getFinalInterestRate());
        app.setDebtServiceRatio(pending.getDebtServiceRatio());

        app.setApplicationStatus(ApplicationStatus.APPLIED);

        return app;
    }


}
