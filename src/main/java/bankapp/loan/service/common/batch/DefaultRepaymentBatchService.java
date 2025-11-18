package bankapp.loan.service.common.batch;

import bankapp.account.model.account.Account;
import bankapp.account.model.account.LoanAccount;
import bankapp.account.request.account.AccountTransactionRequest;
import bankapp.account.service.account.AccountService;
import bankapp.loan.model.common.schedule.RepaymentSchedule;
import bankapp.loan.model.common.schedule.RepaymentStatus;
import bankapp.loan.service.common.schedule.RepaymentScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DefaultRepaymentBatchService implements RepaymentBatchService{

    private final RepaymentScheduleService repaymentScheduleService;
    private final AccountService accountService; // 부모 Account 리포지토리


    @Autowired
    public DefaultRepaymentBatchService(RepaymentScheduleService repaymentScheduleService,
                                        AccountService accountService) {
        this.repaymentScheduleService = repaymentScheduleService;
        this.accountService = accountService;

    }



    /**
     * 매일 새벽 1시 30분에 실행 (초 분 시 일 월 요일)
     * "Good Path" 시나리오만 처리합니다.
     */
    @Scheduled(cron = "0 30 1 * * ?") // 0초 30분 01시 (매일)
    @Override
    @Transactional
    public void processDailyRepayments() {

        // 1. "오늘" 갚아야 하고 "대기 중"인 모든 스케줄 조회
        LocalDate today = LocalDate.now();
        List<RepaymentSchedule> schedulesToProcess =
                repaymentScheduleService.findByRepaymentDateAndStatus(today, RepaymentStatus.PENDING);

        log.info("[Batch] {}건의 상환을 처리합니다. (대상일: {})", schedulesToProcess.size(), today);

        for (RepaymentSchedule schedule : schedulesToProcess) {
            processRepayment(schedule);
        }
    }

    @Transactional
    @Override
    public void processRepayments(LocalDate localDate){
        List<RepaymentSchedule> schedulesToProcess =
                repaymentScheduleService.findByRepaymentDateAndStatus(localDate, RepaymentStatus.PENDING);

        log.info("[Batch] 수동으로 {}건의 상환을 처리합니다. (대상일: {})", schedulesToProcess.size(), localDate);

        for (RepaymentSchedule schedule : schedulesToProcess) {
            processRepayment(schedule);
        }
    }

    /**
     * 개별 스케줄에 대한 상환 처리 로직
     */
    private void processRepayment(RepaymentSchedule schedule) {

        // 1. 필요한 엔티티 조회
        LoanAccount loanAccount = schedule.getLoanAccount();
        Account sourceAccount = loanAccount.getRepaymentAccount(); // [핵심] 고객의 출금 계좌

        BigDecimal principal = schedule.getPrincipalAmount(); // 상환 원금
        BigDecimal interest = schedule.getInterestAmount(); // 상환 이자
        BigDecimal totalAmountToPay = principal.add(interest); // 총 상환액

        if (sourceAccount.getBalance().compareTo(totalAmountToPay) < 0) {
            // todo : 연체 로직 작성
            return ;
        }

        AccountTransactionRequest debitTransaction = new AccountTransactionRequest(
                sourceAccount.getAccountId(),
                totalAmountToPay,
                "대출 상환 출금");
        accountService.debit(debitTransaction);

        // todo : 일단 1번이 코어 뱅킹 계좌라 가정
        AccountTransactionRequest creditTransaction = new AccountTransactionRequest(
                Long.parseLong("1") ,
                totalAmountToPay ,
                "대출 상환 입금");
        accountService.credit(creditTransaction);

        // 대출 계좌에서 원금 잔액 차감

        // 대출 계좌(loanAccount)의 '원금 잔액' 차감
        loanAccount.setBalance(loanAccount.getBalance().subtract(principal));

        // 스케줄 상태를 'PAID' 로 변경
        schedule.setStatus(RepaymentStatus.PAID);
        schedule.setPaidDate(LocalDateTime.now());

    }

}
