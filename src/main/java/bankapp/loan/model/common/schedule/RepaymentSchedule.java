package bankapp.loan.model.common.schedule;


import bankapp.account.model.account.LoanAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repayment_schedule_id")
    private Long repaymentScheduleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private LoanAccount loanAccount;

    @Column(nullable = false , updatable = false)
    private LocalDate repaymentDate;


    @Column(nullable = false , updatable = false)
    private BigDecimal principalAmount; // 상환 원금


    @Column(nullable = false , updatable = false)
    private BigDecimal interestAmount; // 상환 이자

    @Column(nullable = false)
    private Integer repaymentSequence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepaymentStatus status; // 상환 상태 (PENDING, PAID, OVERDUE)

    private LocalDateTime paidDate; // 실제 납입 완료 일시 (PAID 상태일 때)

    @CreationTimestamp
    private LocalDateTime createdAt; // 레코드 생성일

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 레코드 수정일


//
//
//    // (빌더 패턴이나 정적 팩토리 메소드를 사용하여 객체 생성을 관리하는 것을 추천합니다)
//    public static RepaymentSchedule create(LoanAccount loanAccount, LocalDate repaymentDate, BigDecimal principal, BigDecimal interest, int sequence) {
//        RepaymentSchedule schedule = new RepaymentSchedule();
//        schedule.setLoanAccount(loanAccount);
//        schedule.setRepaymentDate(repaymentDate);
//        schedule.setPrincipalAmount(principal);
//        schedule.setInterestAmount(interest);
//        schedule.setTotalAmount(principal.add(interest));
//        schedule.setRepaymentSequence(sequence);
//        schedule.setStatus(RepaymentStatus.PENDING); // 생성 시 기본 상태는 '대기'
//        return schedule;
//    }

    // 연관관계 편의 메서드
    public void setLoanAccount(LoanAccount loanAccount){
        if(this.loanAccount != null){
                this.loanAccount.getRepaymentSchedules().remove(this);
        }
        this.loanAccount = loanAccount;
        this.loanAccount.getRepaymentSchedules().add(this);
    }

}



