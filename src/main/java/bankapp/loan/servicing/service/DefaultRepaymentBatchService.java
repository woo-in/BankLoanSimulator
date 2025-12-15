package bankapp.loan.servicing.service;

import bankapp.account.model.account.LoanAccount;
import bankapp.account.model.account.LoanStatus;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DefaultRepaymentBatchService implements RepaymentBatchService {

    private final RepaymentScheduleService repaymentScheduleService;
    private final Map<LoanStatus, RepaymentStrategy> repaymentStrategyMap;

    @Autowired
    public DefaultRepaymentBatchService(RepaymentScheduleService repaymentScheduleService,
                                       List<RepaymentStrategy> repaymentStrategyList){
        this.repaymentScheduleService = repaymentScheduleService;
        this.repaymentStrategyMap = repaymentStrategyList.stream()
                .collect(java.util.stream.Collectors.toMap(RepaymentStrategy::getLoanStatusType, strategy -> strategy));
    }

    /**
     * 매일 새벽 1시 30분에 실행 (초 분 시 일 월 요일)
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

        LoanAccount loanAccount = schedule.getLoanAccount();
        LoanStatus currentStatus = loanAccount.getLoanStatus(); // LoanAccount에 추가한 상태 필드 사용

        RepaymentStrategy strategy = repaymentStrategyMap.get(currentStatus);

        if (strategy == null) {
            log.error("처리할 수 없는 대출 상태입니다: {}", currentStatus);
            // todo: 적절한 예외 처리 로직 추가
            return;
        }

        // 1. [핵심] 잔액 부족 확인 (공통 로직)
        BigDecimal totalAmountToPay = schedule.getPrincipalAmount().add(schedule.getInterestAmount());
        if (loanAccount.getRepaymentAccount().getBalance().compareTo(totalAmountToPay) < 0) {
            // todo: 잔액 부족 시 DelinquencyService를 호출하여 연체 처리
            // delinquencyService.handleDefault(schedule);
            return;
        }
        strategy.processRepayment(schedule);
    }


}
