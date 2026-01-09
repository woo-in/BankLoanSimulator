package bankapp.loan.underwriting.model;

import bankapp.account.model.account.Account;
import bankapp.account.model.account.LoanAccount;
import bankapp.loan.product.model.LoanProduct;
import bankapp.loan.product.model.InterestRateType;
import bankapp.loan.product.model.RepaymentMethod;
import bankapp.loan.servicing.model.LoanRepaymentTransaction;
import bankapp.loan.servicing.model.LoanStatusHistory;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.member.model.Member;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


@Entity
@Getter
@Setter
@Builder
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@AllArgsConstructor
public class LoanContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanContractId;

    // 기본 정보

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "account_id" , nullable = false)
    private LoanAccount loanAccount;

    // TODO : member , product 에서도 1:다 관계 연결
    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "member_id", nullable = false , updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "loan_product_id", nullable = false , updatable = false)
    private LoanProduct loanProduct;

    // 금리 정보

    @Column(nullable = false)
    private BigDecimal productSpread;

    @Column(nullable = false)
    private BigDecimal creditSpread;

    @Column(nullable = false)
    private BigDecimal baseRate;

    @Column(nullable = false)
    private BigDecimal selectionSpread;

    @Column(nullable = false)
    private BigDecimal finalInterestRate;

    @Column(nullable = false)
    private BigDecimal debtServiceRatio;

    // 선택 정보

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "repayment_method_id", nullable = false)
    private RepaymentMethod repaymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_rate_type_id", nullable = false)
    private InterestRateType interestRateType;

    @Column(nullable = false , updatable = false)
    private BigDecimal loanAmount;

    @Column(nullable = false , updatable = false)
    private Integer loanTerm;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "disbursement_account_id" , nullable = false)
    private Account disbursementAccount;

    // 계약 정보

    @Column(nullable = false , updatable = false)
    private LocalDateTime contractDate;

    @Column(nullable = false , updatable = false)
    private LocalDateTime maturityDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus contractStatus;

    @Column(nullable = false)
    private Integer contractVersion;

    // 레코드 정보

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "loanContract", cascade = CascadeType.ALL)
    private List<RepaymentSchedule> repaymentSchedules = new ArrayList<>();

    @OneToMany(mappedBy = "loanContract", cascade = CascadeType.ALL)
    private List<LoanRepaymentTransaction> loanRepaymentTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "loanContract", cascade = CascadeType.ALL)
    private List<LoanStatusHistory> loanStatusHistories = new ArrayList<>();


    public void setLoanAccount(LoanAccount loanAccount) {
        if(this.loanAccount != null){
            this.loanAccount.getLoanContracts().remove(this);
        }
        this.loanAccount = loanAccount;
        this.loanAccount.getLoanContracts().add(this);
    }

    public void addRepaymentSchedule(RepaymentSchedule schedule) {
        this.repaymentSchedules.add(schedule);
        if (schedule.getLoanContract() != this) {
            schedule.setLoanContract(this);
        }
    }

    public void addLoanRepaymentTransaction(LoanRepaymentTransaction transaction) {
        this.loanRepaymentTransactions.add(transaction);
        if (transaction.getLoanContract() != this) {
            transaction.setLoanContract(this);
        }
    }

    public void addLoanStatusHistory(LoanStatusHistory history) {
        this.loanStatusHistories.add(history);
        if (history.getLoanContract() != this) {
            history.setLoanContract(this);
        }
    }

    /**
     * 승인된 대출 신청서와 생성된 대출 계좌를 기반으로
     * 최초의 대출 계약(Version 1) 엔티티를 생성합니다.
     *
     * @param application 승인된 대출 신청서 (Approved info 포함)
     * @param account     연결될 대출 계좌
     * @return 생성된 LoanContract (Active 상태, Version 1)
     */
    public static LoanContract from(LoanApplication application, LoanAccount account) {
        LoanContract contract = new LoanContract();

        contract.setLoanAccount(account);
        contract.setMember(application.getMember());
        contract.setLoanProduct(application.getLoanProduct());

        contract.setBaseRate(application.getApprovedBaseRate());
        contract.setProductSpread(application.getApprovedProductSpread());
        contract.setCreditSpread(application.getApprovedCreditSpread());
        contract.setSelectionSpread(application.getApprovedSelectionSpread());
        contract.setFinalInterestRate(application.getApprovedFinalInterestRate());
        contract.setDebtServiceRatio(application.getApprovedDebtServiceRatio());

        contract.setLoanAmount(application.getApprovedLoanAmount()); // 승인된 금액
        contract.setLoanTerm(application.getApprovedLoanTerm());     // 승인된 기간
        contract.setRepaymentMethod(application.getRepaymentMethod());
        contract.setInterestRateType(application.getInterestRateType());
        contract.setDisbursementAccount(application.getDisbursementAccount());

        LocalDateTime now = LocalDateTime.now();
        contract.setContractDate(now); // 계약 체결일 = 현재

        // 만기일 계산 = 시작일 + 대출기간(개월)
        contract.setMaturityDate(now.plusMonths(application.getApprovedLoanTerm()));

        contract.setContractStatus(ContractStatus.ACTIVE); // 현재 유효한 계약
        contract.setContractVersion(1); // 최초 계약이므로 버전 1

        return contract;
    }

}
