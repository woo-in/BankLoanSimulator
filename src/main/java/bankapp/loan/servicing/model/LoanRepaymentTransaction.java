package bankapp.loan.servicing.model;

import bankapp.account.model.account.LoanAccount;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "account_id" , nullable = false)
    private LoanAccount loanAccount;

    // --- [입금 정보] ---
    @Column(nullable = false)
    private BigDecimal transactionAmount; // 고객이 입금한 총 금액

    @Column(nullable = false)
    private LocalDateTime transactionDate; // 거래 일시



    // --- [충당(Allocation) 내역 - 돈이 어디로 사라졌나] ---

    // 1순위: 연체 가산 이자 상환액
    @Column(nullable = false)
    private BigDecimal paidPenaltyAmount;

    // 2순위: 연체되었던 '이자' 상환액
    @Column(nullable = false)
    private BigDecimal paidOverdueInterestAmount;

    // 3순위: 연체되었던 '원금' 상환액
    @Column(nullable = false)
    private BigDecimal paidOverduePrincipalAmount;

    // 4순위: (연체가 다 풀리고 남은 돈으로) 정상 이자 상환액
    @Column(nullable = false)
    private BigDecimal paidScheduledInterestAmount;

    // 5순위: 정상 원금 상환액
    @Column(nullable = false)
    private BigDecimal paidScheduledPrincipalAmount;

    // --- [잔액 스냅샷 (선택사항)] ---
    private BigDecimal loanBalanceAfterTransaction; // 거래 후 대출 잔액


    public void setLoanAccount(LoanAccount loanAccount){
        if(this.loanAccount != null){
                this.loanAccount.getLoanRepaymentTransactions().remove(this);
        }
        this.loanAccount = loanAccount;
        this.loanAccount.getLoanRepaymentTransactions().add(this);
    }

}
