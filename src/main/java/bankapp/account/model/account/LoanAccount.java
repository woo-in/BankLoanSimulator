package bankapp.account.model.account;


import bankapp.account.request.open.OpenLoanAccountRequest;
import bankapp.account.request.open.OpenPrimaryAccountRequest;
import bankapp.loan.model.common.contract.LoanContract;
import bankapp.member.model.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("LOAN")
@NoArgsConstructor
public class LoanAccount extends Account {


    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "loanAccount")
    private List<LoanContract> loanContracts = new ArrayList<>();

    public LoanAccount(Member member, String accountNumber, BigDecimal balance, String nickname, AccountStatus status) {
        super(member, accountNumber, balance, nickname,status);
    }

    public LoanAccount(Member member, String accountNumber, BigDecimal balance, AccountStatus status) {
        super(member, accountNumber, balance, status);
    }

    public static LoanAccount from(OpenLoanAccountRequest openLoanAccountRequest, Member member, String accountNumber)  {
        return new LoanAccount(member , accountNumber , openLoanAccountRequest.getBalance() , openLoanAccountRequest.getNickname() , AccountStatus.ACTIVE);
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
