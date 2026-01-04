package bankapp.account.model.account;


import bankapp.loan.underwriting.model.LoanApplication;
import bankapp.member.model.Member;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;


@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("LOAN")
@NoArgsConstructor
public class LoanAccount extends Account {


    // --- [필드 정의] ---

    // 1. 상환 관련 변동 가능 정보
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repayment_account_id", nullable = false)
    private Account repaymentAccount; // 자동이체 출금 계좌 (변경 가능)

    @Column(nullable = false)
    private Integer paymentDay; // 매월 결제일 (변경 가능)


    // 2. 기록용 불변 정보
    @Column(name = "disbursement_account_number")
    private String disbursementAccountNumber; // 최초 입금받은 계좌번호 (기록용)

    // 3. 대출 상태 관리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus loanStatus; // NORMAL, OVERDUE, PAID_OFF 등

    // --- [생성자 (Builder 패턴 대용)] ---
    @Builder
    public LoanAccount(Member member, String accountNumber, BigDecimal balance, String nickname, AccountStatus status,
                       Account repaymentAccount, Integer paymentDay, String disbursementAccountNumber, LoanStatus loanStatus) {
        super(member, accountNumber, balance, nickname, status); // 부모(Account) 필드 초기화
        this.repaymentAccount = repaymentAccount;
        this.paymentDay = paymentDay;
        this.disbursementAccountNumber = disbursementAccountNumber;
        this.loanStatus = loanStatus;
    }

    // --- [핵심: 정적 팩토리 메서드] ---
    /**
     * LoanApplication(신청서) 정보를 바탕으로 LoanAccount(계좌)를 생성합니다.
     * @param application 승인된 대출 신청서
     * @param newAccountNumber 생성된 계좌번호
     */
    public static LoanAccount createFrom(LoanApplication application, String newAccountNumber) {
        return LoanAccount.builder()
                .member(application.getMember())
                .accountNumber(newAccountNumber)
                // 대출 계좌의 잔액 = 대출 원금 (양수로 관리한다고 가정, 상환 시 줄어듦)
                .balance(application.getApprovedLoanAmount())
                .nickname(application.getLoanProduct().getLoanProductName()) // 상품명을 닉네임으로
                .status(AccountStatus.ACTIVE) // 계좌 상태 활성

                // 가변 정보 매핑
                .repaymentAccount(application.getRepaymentAccount())
                .paymentDay(application.getPaymentDay())
                .disbursementAccountNumber(application.getDisbursementAccount().getAccountNumber())

                .loanStatus(LoanStatus.NORMAL) // 대출 상태 정상
                .build();
    }




}