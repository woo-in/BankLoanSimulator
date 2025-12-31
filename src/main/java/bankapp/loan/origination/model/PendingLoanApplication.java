package bankapp.loan.origination.model;

import bankapp.loan.product.model.InterestRateType;
import bankapp.loan.product.model.LoanProduct;
import bankapp.loan.product.model.RepaymentMethod;
import bankapp.member.model.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pending_loan_application")
public class PendingLoanApplication {

    // 기본 정보
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pendingLoanApplicationId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    // 유저 재무 정보
    private BigDecimal totalAssets;
    private BigDecimal annualIncome;
    private BigDecimal fixedExpenses;
    @Builder.Default
    @OneToMany(mappedBy = "pendingLoanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExistingLoan> existingLoans = new ArrayList<>();

    // 신청 대출 조건
    private BigDecimal requestLoanAmount;
    private Integer requestLoanTerm;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repayment_method_id")
    private RepaymentMethod repaymentMethod;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_rate_type_id")
    private InterestRateType interestRateType;

    // 진행 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PendingLoanApplicationStatus status;


    // 금리 정보
    private BigDecimal baseRate;
    private BigDecimal productSpread;
    private BigDecimal creditSpread;
    private BigDecimal selectionSpread;
    private BigDecimal finalInterestRate;
    private BigDecimal debtServiceRatio;


    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}