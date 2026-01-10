package bankapp.account.model.account;


import bankapp.loan.servicing.model.LoanRepaymentTransaction;
import bankapp.loan.servicing.model.LoanStatusHistory;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.underwriting.model.LoanApplication;
import bankapp.loan.underwriting.model.LoanContract;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repayment_account_id", nullable = false)
    private Account repaymentAccount;

    @Column(nullable = false)
    private Integer paymentDay;

    // 연체와 관련 없는 정보

    // 회차
    @Column(nullable = false)
    private Integer currentInstallmentNumber;

    // 예상 원금
    @Column(nullable = false)
    private BigDecimal outstandingPrincipal;

    //

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus loanStatus;

    @OneToMany(mappedBy = "loanAccount", cascade = CascadeType.ALL)
    private List<LoanContract> loanContracts = new ArrayList<>();

    @OneToMany(mappedBy = "loanAccount", cascade = CascadeType.ALL)
    private List<RepaymentSchedule> repaymentSchedules = new ArrayList<>();

    @OneToMany(mappedBy = "loanAccount", cascade = CascadeType.ALL)
    private List<LoanRepaymentTransaction> loanRepaymentTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "loanAccount", cascade = CascadeType.ALL)
    private List<LoanStatusHistory> loanStatusHistories = new ArrayList<>();

    // todo : 굳이 ?
    public void addLoanContract(LoanContract contract) {
        this.loanContracts.add(contract);
        if (contract.getLoanAccount() != this) {
            contract.setLoanAccount(this);
        }
    }

    public void addRepaymentSchedule(RepaymentSchedule schedule) {
        this.repaymentSchedules.add(schedule);
        if (schedule.getLoanAccount() != this) {
            schedule.setLoanAccount(this);
        }
    }

    public void addLoanRepaymentTransaction(LoanRepaymentTransaction transaction) {
        this.loanRepaymentTransactions.add(transaction);
        if (transaction.getLoanAccount() != this) {
            transaction.setLoanAccount(this);
        }
    }

    public void addLoanStatusHistory(LoanStatusHistory history) {
        this.loanStatusHistories.add(history);
        if (history.getLoanAccount() != this) {
            history.setLoanAccount(this);
        }
    }

    /**
     * 승인된 대출 신청서(LoanApplication)와 생성된 계좌번호를 받아
     * 활성 상태의 LoanAccount 엔티티를 생성합니다.
     *
     * @param loanApplication 승인된 대출 신청서
     * @param accountNumber   새로 생성된 대출 계좌번호
     * @return 생성된 LoanAccount
     */
    public static LoanAccount from(LoanApplication loanApplication, String accountNumber) {
        LoanAccount account = new LoanAccount();

        account.setMember(loanApplication.getMember());
        account.setAccountNumber(accountNumber);
        account.setBalance(loanApplication.getApprovedLoanAmount());
        account.setNickname(loanApplication.getLoanProduct().getLoanProductName());
        account.setStatus(AccountStatus.ACTIVE);
        account.setRepaymentAccount(loanApplication.getRepaymentAccount());
        account.setPaymentDay(loanApplication.getPaymentDay());
        account.setCurrentInstallmentNumber(1);
        account.setOutstandingPrincipal(loanApplication.getApprovedLoanAmount());
        account.setLoanStatus(LoanStatus.NORMAL);

        return account;
    }





}