package bankapp.loan.servicing.model;

import bankapp.account.model.account.LoanAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OverdueRepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long overdueRepaymentScheduleId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repayment_schedule_id" , nullable = false)
    private RepaymentSchedule repaymentSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private LoanAccount loanAccount;

    // --- [연체된 원금과 이자 (고정값)] ---
    // 원래 스케줄에서 못 갚은 원금
    @Column(nullable = false , updatable = false)
    private BigDecimal overduePrincipal;

    // 원래 스케줄에서 못 갚은 이자
    @Column(nullable = false , updatable = false)
    private BigDecimal overdueInterest;

    // --- [매일 변하는 값] ---
    // 현재까지 누적된 연체 가산 이자 (매일 배치로 update)
    // 계산식: (overduePrincipal + overdueInterest) * 연체이율 * 경과일수
    @Column(nullable = false)
    private BigDecimal accumulatedPenalty;

    // --- [관리 정보] ---
    @Column(nullable = false)
    private LocalDate overdueStartDate; // 연체 시작일 (원래 납입일 다음날)

    private LocalDate overdueEndDate;   // 연체 해소일 (전액 상환 시 기록)

    @Enumerated(EnumType.STRING)
    private OverdueStatus status; // ACTIVE(연체중), RESOLVED(해소됨)


    // 연관관계 편의 메서드
    public void setLoanAccount(LoanAccount loanAccount){
        if(this.loanAccount != null){
                this.loanAccount.getOverdueRepaymentSchedules().remove(this);
        }
        this.loanAccount = loanAccount;
        this.loanAccount.getOverdueRepaymentSchedules().add(this);
    }


}
