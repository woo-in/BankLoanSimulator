package bankapp.loan.origination.model;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_loan_application_id")
    private PendingLoanApplication pendingLoanApplication;

    @Column(nullable = false)
    private String loanProductName;

    @Column(nullable = false)
    private String loanType;

    @Column(nullable = false)
    private BigDecimal loanAmount;

    @Column(nullable = false)
    private Integer loanTerm;

    @Column(nullable = false)
    private String repaymentMethodName;

    @Column(nullable = false)
    private BigDecimal totalInterestRate;

    @Column(nullable = false)
    private boolean isExternal;


    public void setPendingLoanApplication(PendingLoanApplication pendingLoanApplication) {
        if(this.pendingLoanApplication != null){
            this.pendingLoanApplication.getExistingLoans().remove(this);
        }
        this.pendingLoanApplication = pendingLoanApplication;
        this.pendingLoanApplication.getExistingLoans().add(this);
    }


}