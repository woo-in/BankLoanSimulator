package bankapp.account.model.account;


import bankapp.account.request.open.OpenLoanAccountRequest;
import bankapp.loan.model.common.contract.LoanContract;
import bankapp.loan.model.common.schedule.RepaymentSchedule;
import bankapp.member.model.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("LOAN")
@NoArgsConstructor
public class LoanAccount extends Account {


    // todo : 상환 출금 계좌 바꾸기 기능 추가
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repayment_account_id", nullable = false)
    private Account repaymentAccount;


    @OneToMany(
            mappedBy = "loanAccount",
            fetch = FetchType.LAZY
    )
    private List<LoanContract> loanContracts = new ArrayList<>();

    @OneToMany(
            mappedBy = "loanAccount",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<RepaymentSchedule> repaymentSchedules = new ArrayList<>();

    public LoanAccount(Account repaymentAccount, Member member, String accountNumber, BigDecimal balance, String nickname, AccountStatus status) {
        super(member, accountNumber, balance, nickname, status);
        this.repaymentAccount = repaymentAccount;
    }

    public static LoanAccount from(OpenLoanAccountRequest openLoanAccountRequest,
                                   Member member,
                                   String accountNumber)  {

        // 수정된 생성자를 호출
        return new LoanAccount(
                openLoanAccountRequest.getRepaymentAccount(),
                member,
                accountNumber,
                openLoanAccountRequest.getBalance(),
                openLoanAccountRequest.getNickname(),
                AccountStatus.ACTIVE
        );
    }


//     미상환 이자
//    @Column(nullable = false)
//    private BigDecimal outstandingInterest = BigDecimal.ZERO;

//    //
//    @Column(nullable = false)
//    private BigDecimal totalOverdueAmount = BigDecimal.ZERO;

    // todo : 다음에 언제 갚을지 예정일
//    @Column(nullable = false)
//    private LocalDateTime nextPaymentDate;

    // todo : 연속 연체 횟수
//    @Column(nullable = false)
//    private Integer consecutiveOverdueCount ;
}
