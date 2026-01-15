package bankapp.loan.servicing.service.process;

import bankapp.loan.servicing.model.LoanAccount;
import bankapp.loan.servicing.model.LoanStatus;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;
import bankapp.loan.servicing.service.core.LoanAccountService;
import bankapp.loan.servicing.service.core.RepaymentScheduleService;
import bankapp.loan.servicing.service.core.RepaymentTransactionService;
import bankapp.loan.servicing.service.lifecycle.LoanStatusService;
import bankapp.loan.servicing.service.lifecycle.RepaymentStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DefaultLoanAgingService implements LoanAgingService {


    /**
     * 상환 예정일(Due Date) 며칠 전부터 스케줄을 '대기(PENDING)' 상태로 활성화할지 결정하는 기준 일수입니다.
     * <p>
     * 이 기간 내에 들어온 스케줄은 PLANNED -> PENDING으로 전환되어
     * 상환 시스템의 관리 대상(알림 발송, 자동 이체 시도 등)이 됩니다.
     * </p>
     */
    private static final int PENDING_ENTRY_THRESHOLD_DAYS = 5;

    private final LoanAccountService loanAccountService;
    private final RepaymentScheduleService repaymentScheduleService;
    private final RepaymentTransactionService repaymentTransactionService;
    private final RepaymentStatusService repaymentStatusService;
    private final LoanStatusService loanStatusService;

    @Autowired
    public DefaultLoanAgingService(LoanAccountService loanAccountService,
                                   RepaymentScheduleService repaymentScheduleService,
                                   RepaymentTransactionService repaymentTransactionService,
                                   RepaymentStatusService repaymentStatusService,
                                   LoanStatusService loanStatusService) {
        this.loanAccountService = loanAccountService;
        this.repaymentScheduleService = repaymentScheduleService;
        this.repaymentTransactionService = repaymentTransactionService;
        this.repaymentStatusService = repaymentStatusService;
        this.loanStatusService = loanStatusService;
    }

    // -- 외부 호출
    @Override
    @Transactional
    public void processDailyAging(LocalDate targetDate){
        // 1. 반드시 상태 변화 (먼저 실행)
        transitionLoanLifecycle(targetDate);
        // 2. 금액 갱신
        updateOverdueBalances();
    }

    // --- 중간 관리 ---

    private void transitionLoanLifecycle(LocalDate targetDate) {
        transitionToPending(targetDate);            // 1. 기한 도래
        transitionToDelinquent(targetDate);         // 5. 연체 진입
        transitionToAccelerationNotice(targetDate); // 6. 예고 진입
        transitionToAcceleration(targetDate);       // 7. 상실 확정
    }

    private void updateOverdueBalances() {
        updateBalanceForDelinquent();         // 2. DELINQUENT 상태 처리
        updateBalanceForAccelerationNotice(); // 3. ACC_NOTICE 상태 처리
        updateBalanceForAcceleration();       // 4. ACCELERATION 상태 처리
    }

    // --- (상태 변화) ---
    private void transitionToPending(LocalDate targetDate) {
        LocalDate billingHorizon = targetDate.plusDays(PENDING_ENTRY_THRESHOLD_DAYS);

        List<RepaymentSchedule> transitionToPendingSchedules = repaymentScheduleService.findSchedulesByStatusAndDueDate(
                RepaymentStatus.PLANNED,
                billingHorizon
        );
        for(RepaymentSchedule transitionToPendingSchedule : transitionToPendingSchedules){
            // [스케줄 상태 변경]
            repaymentScheduleService.updateAmount(transitionToPendingSchedule);
            repaymentScheduleService.updateRepaymentStatus(transitionToPendingSchedule , RepaymentStatus.PENDING);

            // [계좌 상태 변경]
            LoanAccount loanAccount = transitionToPendingSchedule.getLoanAccount();
            BigDecimal newOutstandingPrincipal = loanAccount.getOutstandingPrincipal().subtract(transitionToPendingSchedule.getPrincipalAmount());
            loanAccountService.updateLoanProgress(loanAccount,newOutstandingPrincipal);
        }
    }


    private void transitionToDelinquent(LocalDate targetDate) {

        List<RepaymentSchedule> transitionToOverdueSchedules = repaymentScheduleService.findSchedulesByLoanStatusAndRepaymentStatusAndDueDate(
                LoanStatus.NORMAL,
                RepaymentStatus.PENDING,
                targetDate
        );

        for(RepaymentSchedule transitionToOverdueSchedule : transitionToOverdueSchedules){

            // 스케줄링 status 바꾸기
            // todo : 상태 변화에 대한 건 , repaymentScheduleService 최대한 쓰면 안된다.(수정 사항 1)
            repaymentStatusService.changeRepaymentStatus(transitionToOverdueSchedule , RepaymentStatus.OVERDUE);

            // loanStatus 바꾸기 (반드시 스케줄링 먼저 하고 바꿔야 함)
            // todo : 설계를 바꿈에 따라  DefaultLoanStatusService 변경 (수정 사항 2)

        }



    }
    private void transitionToAccelerationNotice(LocalDate targetDate) {}
    private void transitionToAcceleration(LocalDate targetDate) {}

    // --- (금액 갱신) ---
    private void updateBalanceForDelinquent() {
        List<RepaymentSchedule> delinquentRepaymentSchedules = repaymentScheduleService.findSchedulesByLoanAndRepaymentStatus(
                LoanStatus.DELINQUENT,
                RepaymentStatus.OVERDUE);

        for(RepaymentSchedule delinquentRepaymentSchedule : delinquentRepaymentSchedules){
            repaymentScheduleService.updateDailyDelinquent(delinquentRepaymentSchedule);
        }
    }
    private void updateBalanceForAccelerationNotice() {
        List<RepaymentSchedule> noticeSchedules = repaymentScheduleService.findSchedulesByLoanAndRepaymentStatus(
                LoanStatus.ACCELERATION_NOTICE,
                RepaymentStatus.CRITICAL_OVERDUE
        );

        for (RepaymentSchedule schedule : noticeSchedules) {
            repaymentScheduleService.updateDailyAcceleration(schedule);
        }
    }
    private void updateBalanceForAcceleration() {
        List<RepaymentSchedule> accelerationSchedules = repaymentScheduleService.findSchedulesByLoanAndRepaymentStatus(
                LoanStatus.ACCELERATION,
                RepaymentStatus.ACCELERATED
        );

        for (RepaymentSchedule schedule : accelerationSchedules) {
            repaymentScheduleService.updateDailyAcceleration(schedule);
        }
    }




    // 모든 로직이 매일 배치를 돌며 , RepaymentSchedule 에서 탐색 , 조건 만족시 변경의 절차로 이루어짐
    // 만약 상환상태 변경한다면 북마크 (2) 와 잘 조율 해야 함
    // 5. NORMAL -> DELINQUENT / PENDING -> OVERDUE
    // 6. DELINQUENT -> ACC_NOTICE / PENDING -> OVERDUE
    // 7. ACC_NOTICE -> ACC / OVERDUE , PENDING , PLANNED -> MERGE (당장 다 갚아라)









}
