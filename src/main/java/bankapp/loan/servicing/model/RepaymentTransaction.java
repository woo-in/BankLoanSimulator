package bankapp.loan.servicing.model;

import bankapp.loan.underwriting.model.LoanContract;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "account_id" , nullable = false)
    private LoanAccount loanAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_contract_id", nullable = false)
    private LoanContract loanContract;


    // 상환 정보

    @Column(nullable = false)
    private LocalDateTime transactionDate;
    private BigDecimal interestAmount;
    private BigDecimal principalAmount;
    private BigDecimal delinquentAmount;
    private BigDecimal accelerationPenaltyAmount;
    private BigDecimal totalRepaymentAmount;
    private BigDecimal loanBalanceAfterTransaction;

    // 컬럼 정보

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;



    /**
     * 대출 계좌 설정 및 양방향 관계 맺기
     */
    public void setLoanAccount(LoanAccount loanAccount) {
        // 1. 기존 관계 제거 (필요 시)
        if (this.loanAccount != null) {
            this.loanAccount.getRepaymentTransactions().remove(this);
        }
        // 2. 새로운 관계 설정
        this.loanAccount = loanAccount;
        // 3. 반대쪽 리스트에 추가 (무한루프 방지 체크)
        if (loanAccount != null && !loanAccount.getRepaymentTransactions().contains(this)) {
            loanAccount.getRepaymentTransactions().add(this);
        }
    }

    /**
     * 대출 계약 설정 및 양방향 관계 맺기
     */
    public void setLoanContract(LoanContract loanContract) {
        // 1. 기존 관계 제거
        if (this.loanContract != null) {
            this.loanContract.getRepaymentTransactions().remove(this);
        }
        // 2. 새로운 관계 설정
        this.loanContract = loanContract;
        // 3. 반대쪽 리스트에 추가
        if (loanContract != null && !loanContract.getRepaymentTransactions().contains(this)) {
            loanContract.getRepaymentTransactions().add(this);
        }
    }



}
