package bankapp.loan.origination.model;

import bankapp.loan.underwriting.model.LoanApplication; // [추가]
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "existing_loan")
public class ExistingLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long existingLoanId;

    // 기존: 신청서(Pending)와의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_loan_application_id")
    private PendingLoanApplication pendingLoanApplication;

    // [추가] 심사서(LoanApplication)와의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id")
    private LoanApplication loanApplication;

    @Column(nullable = false)
    private String loanProductName;

    @Column(nullable = false)
    private String loanType;

    @Column(nullable = false)
    private BigDecimal loanAmount;

    @Column(nullable = false)
    private BigDecimal remainingBalance;

    @Column(nullable = false)
    private Integer loanTerm;

    @Column(nullable = false)
    private String repaymentMethodName;

    @Column(nullable = false)
    private String interestRateTypeName;

    @Column(nullable = false)
    private BigDecimal totalInterestRate;

    @Column(nullable = false)
    private boolean isExternal;

    // Pending 연결 편의 메서드
    public void setPendingLoanApplication(PendingLoanApplication pendingLoanApplication) {
        if(this.pendingLoanApplication != null){
            this.pendingLoanApplication.getExistingLoans().remove(this);
        }
        this.pendingLoanApplication = pendingLoanApplication;
        this.pendingLoanApplication.getExistingLoans().add(this);
    }

    // [추가] LoanApplication 연결 편의 메서드
    public void setLoanApplication(LoanApplication loanApplication) {
        if(this.loanApplication != null){
            this.loanApplication.getExistingLoans().remove(this);
        }
        this.loanApplication = loanApplication;
        this.loanApplication.getExistingLoans().add(this);
    }
}