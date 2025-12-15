package bankapp.loan.servicing.service;


import bankapp.account.model.account.Account;
import bankapp.account.model.account.LoanAccount;
import bankapp.account.model.account.LoanStatus;
import bankapp.account.request.account.AccountTransactionRequest;
import bankapp.account.service.account.AccountService;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class NormalRepaymentStrategy implements RepaymentStrategy {

    private final AccountService accountService;
    private final RepaymentTransactionService repaymentTransactionService;
    // private final DelinquencyService delinquencyService; // 연체 발생 시 호출 예정

    @Autowired
    public NormalRepaymentStrategy(AccountService accountService,
                                   RepaymentTransactionService repaymentTransactionService) {
        this.accountService = accountService;
        this.repaymentTransactionService = repaymentTransactionService;
    }


    @Override
    public LoanStatus getLoanStatusType() {
        return LoanStatus.NORMAL;
    }

    @Override
    @Transactional
    public void processRepayment(RepaymentSchedule schedule) {

        LoanAccount loanAccount = schedule.getLoanAccount();
        Account sourceAccount = loanAccount.getRepaymentAccount(); // 고객의 출금 계좌

        BigDecimal principal = schedule.getPrincipalAmount(); // 상환 원금
        BigDecimal interest = schedule.getInterestAmount(); // 상환 이자
        BigDecimal totalAmountToPay = principal.add(interest); // 총 상환액

        // --- 1. 잔액 확인 (성공 가정) ---
        if (sourceAccount.getBalance().compareTo(totalAmountToPay) < 0) {
            // 잔액 부족 시: 연체 처리 로직을 담당하는 DelinquencyService를 호출해야 함
            // delinquencyService.handleDefault(schedule);
            log.warn("잔액 부족으로 상환 실패: LoanAccount ID {}", loanAccount.getAccountId());
            return;
        }

        // --- 2. 출금/입금 처리 (거래 성공) ---

        // 1) 고객 출금 계좌에서 출금 (Debit)
        AccountTransactionRequest debitTransaction = new AccountTransactionRequest(
                sourceAccount.getAccountId(),
                totalAmountToPay,
                "정상 대출 상환 출금");
        accountService.debit(debitTransaction);

        // 2) 은행 내부 계좌로 입금 (Credit)
        // (실제 은행 시스템에서는 코어 뱅킹의 특정 계좌로 입금 처리)
        AccountTransactionRequest creditTransaction = new AccountTransactionRequest(
                1L , // 가상의 은행 코어 계좌 ID
                totalAmountToPay ,
                "정상 대출 상환 입금");
        accountService.credit(creditTransaction);

        // 3) 대출 계좌 및 스케줄 업데이트

        // 대출 계좌(loanAccount)의 원금 잔액 차감
        loanAccount.setBalance(loanAccount.getBalance().subtract(principal));

        // 스케줄 상태를 'PAID' 로 변경
        schedule.setStatus(RepaymentStatus.PAID);
        schedule.setPaidDate(LocalDateTime.now());

        // 4) 대출 상환 내역 업데이트
        repaymentTransactionService.createNormalTransaction(loanAccount,totalAmountToPay,interest,principal);

        log.info("정상 상환 성공: LoanAccount ID {}, Schedule ID {}",
                loanAccount.getAccountId(), schedule.getRepaymentScheduleId());
    }


}
