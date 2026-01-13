package bankapp.loan.servicing.model;


import bankapp.loan.underwriting.model.LoanContract;
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

    // 계좌 , 계약 정보

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private LoanAccount loanAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_contract_id", nullable = false)
    private LoanContract loanContract;

    // 스케줄 금액
    private BigDecimal totalAmount;
    private BigDecimal interestAmount; // 이자
    private BigDecimal principalAmount; // 원금
    private BigDecimal delinquentAmount; // DELINQUENT 연체이자
    private BigDecimal accelerationPenaltyAmount; // ACCELERATION , ACCELERATION_NOTICE 연체이자
    private BigDecimal appliedInterestRate; // 적용된 금리 정보 (확정)

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepaymentStatus status; // 상환 상태

    @CreationTimestamp
    private LocalDateTime createdAt; // 레코드 생성일

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 레코드 수정일


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
        if (loanAccount != null && !loanAccount.getRepaymentSchedules().contains(this)) {
            loanAccount.getRepaymentSchedules().add(this);
        }
    }

    public void setLoanContract(LoanContract loanContract) {
        if (this.loanContract != null) {
            this.loanContract.getRepaymentSchedules().remove(this);
        }
        this.loanContract = loanContract;
        if (loanContract != null && !loanContract.getRepaymentSchedules().contains(this)) {
            loanContract.getRepaymentSchedules().add(this);
        }
    }



}



