package bankapp.loan.servicing.service.process;

import bankapp.loan.servicing.model.LoanAccount;
import bankapp.loan.servicing.model.LoanStatus;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;
import bankapp.loan.servicing.service.core.LoanAccountService;
import bankapp.loan.servicing.service.core.RepaymentScheduleService;
import bankapp.loan.servicing.service.lifecycle.LoanStatusService;
import bankapp.loan.servicing.service.lifecycle.RepaymentStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    // DefaultRepaymentStatusService 와만 공유
    private static final int DAYS_BEFORE_FOR_PENDING = 5;
    private static final int MONTHS_AFTER_FOR_CRITICAL_OVERDUE = 1;
    private static final int MONTHS_AFTER_FOR_ACCELERATED = 2;
    private static final int MONTHS_AFTER_FOR_MERGED = 1;


    private final RepaymentScheduleService repaymentScheduleService;
    private final RepaymentStatusService repaymentStatusService;
    private final LoanAccountService loanAccountService;
    private final LoanStatusService loanStatusService;

    @Autowired
    public DefaultLoanAgingService(RepaymentScheduleService repaymentScheduleService,
                                   RepaymentStatusService repaymentStatusService, LoanAccountService loanAccountService, LoanStatusService loanStatusService){
        this.repaymentScheduleService = repaymentScheduleService;
        this.repaymentStatusService = repaymentStatusService;
        this.loanAccountService = loanAccountService;
        this.loanStatusService = loanStatusService;
    }


    @Override
    @Transactional
    public void processDailyAging(LocalDate targetDate){

        //1. RS 변경 배치 , merge 작업
        transitionRepaymentStatus(targetDate);
        mergeBalanceForAcceleration();

        //2. LS 변경 배치
        transitionLoanStatus();

        // 3. 금액 갱신
        updateOverdueBalances();
    }



    private void transitionRepaymentStatus(LocalDate targetDate) {
        // 모든 요소에 대해 검색 후 처리
        transitionStatusToPending(targetDate);
        transitionStatusToOverdue(targetDate);
        transitionStatusToCriticalOverdue(targetDate);
        transitionStatusToAccelerated(targetDate);
        transitionStatusToMerged(targetDate);
    }
    private void transitionStatusToPending(LocalDate targetDate) {
        // 1. 대상 LoanStatus 목록 (NORMAL, DELINQUENT, ACCELERATION_NOTICE)
        List<LoanStatus> targetLoanStatuses = List.of(
                LoanStatus.NORMAL,
                LoanStatus.DELINQUENT,
                LoanStatus.ACCELERATION_NOTICE
        );

        // 2. 검색 기준 날짜 계산 (오늘 + 5일 >= 만기일 인 건을 조회)
        LocalDate criteriaDate = targetDate.plusDays(DAYS_BEFORE_FOR_PENDING);

        // 3. 검색 (1차 필터링)
        List<RepaymentSchedule> schedules = repaymentScheduleService.findSchedulesByLoanStatusesAndRepaymentStatusAndDueDate(
                targetLoanStatuses,
                RepaymentStatus.PLANNED,
                criteriaDate
        );

        // 4. 로직 적용 (2차 검증 및 상태 변경)
        for (RepaymentSchedule schedule : schedules) {
            repaymentStatusService.changeRepaymentStatusToPending(schedule, targetDate);
        }
    }
    private void transitionStatusToOverdue(LocalDate targetDate) {
        // 1. 대상 LoanStatus 목록 (NORMAL, DELINQUENT)
        // 주의: ACCELERATION_NOTICE 상태는 이미 독촉 중이므로 일반 연체 로직 제외
        List<LoanStatus> targetLoanStatuses = List.of(
                LoanStatus.NORMAL,
                LoanStatus.DELINQUENT
        );

        // 2. 검색 기준 날짜 (만기일이 오늘과 같거나 이전이면 연체)
        // 로직: DueDate <= targetDate

        // 3. 검색 (PENDING 상태인 것들 중 기한 지난 것 찾기)
        List<RepaymentSchedule> schedules = repaymentScheduleService.findSchedulesByLoanStatusesAndRepaymentStatusAndDueDate(
                targetLoanStatuses,
                RepaymentStatus.PENDING,
                targetDate
        );

        // 4. 로직 적용
        for (RepaymentSchedule schedule : schedules) {
            repaymentStatusService.changeRepaymentStatusToOverdue(schedule, targetDate);
        }
    }
    private void transitionStatusToCriticalOverdue(LocalDate targetDate) {
        // 1. 대상 LoanStatus (DELINQUENT만 해당)
        List<LoanStatus> targetLoanStatuses = List.of(LoanStatus.DELINQUENT);

        // 2. 검색 기준 날짜 계산
        // 로직: DueDate + 1개월 <= targetDate  ->  DueDate <= targetDate - 1개월
        // 상수를 사용하여 유지보수성 확보 (DefaultRepaymentStatusService의 public 상수 사용)
        LocalDate criteriaDate = targetDate.minusMonths(MONTHS_AFTER_FOR_CRITICAL_OVERDUE);

        // 3. 검색 (OVERDUE 상태 중 1개월 지난 것 찾기)
        List<RepaymentSchedule> schedules = repaymentScheduleService.findSchedulesByLoanStatusesAndRepaymentStatusAndDueDate(
                targetLoanStatuses,
                RepaymentStatus.OVERDUE,
                criteriaDate
        );

        // 4. 로직 적용
        for (RepaymentSchedule schedule : schedules) {
            repaymentStatusService.changeRepaymentStatusToCriticalOverdue(schedule, targetDate);
        }
    }
    private void transitionStatusToAccelerated(LocalDate targetDate) {
        // 1. 대상 LoanStatus (ACCELERATION_NOTICE 상태만 해당)
        List<LoanStatus> targetLoanStatuses = List.of(LoanStatus.ACCELERATION_NOTICE);

        // 2. 검색 기준 날짜 계산
        // 로직: DueDate + 2개월 <= targetDate  ->  DueDate <= targetDate - 2개월
        LocalDate criteriaDate = targetDate.minusMonths(MONTHS_AFTER_FOR_ACCELERATED);

        // 3. 검색 (CRITICAL_OVERDUE 상태 중 2개월 지난 것 찾기)
        List<RepaymentSchedule> schedules = repaymentScheduleService.findSchedulesByLoanStatusesAndRepaymentStatusAndDueDate(
                targetLoanStatuses,
                RepaymentStatus.CRITICAL_OVERDUE,
                criteriaDate
        );

        // 4. 로직 적용
        for (RepaymentSchedule schedule : schedules) {
            repaymentStatusService.changeRepaymentStatusToAccelerated(schedule, targetDate);
        }
    }
    private void transitionStatusToMerged(LocalDate targetDate) {
        // 공통 대상 LoanStatus (ACCELERATION_NOTICE 상태만 해당)
        List<LoanStatus> targetLoanStatuses = List.of(LoanStatus.ACCELERATION_NOTICE);

        // -------------------------------------------------------
        // [조건 A] : OVERDUE 상태 + 1개월 경과
        // 로직: DueDate <= targetDate - 1개월
        // -------------------------------------------------------
        LocalDate criteriaDateA = targetDate.minusMonths(MONTHS_AFTER_FOR_MERGED);

        List<RepaymentSchedule> schedulesA = repaymentScheduleService.findSchedulesByLoanStatusesAndRepaymentStatusAndDueDate(
                targetLoanStatuses,
                RepaymentStatus.OVERDUE,
                criteriaDateA
        );

        for (RepaymentSchedule schedule : schedulesA) {
            repaymentStatusService.changeRepaymentStatusToMerged(schedule, targetDate);
        }

        // -------------------------------------------------------
        // [조건 B] : PENDING 상태 + 만기일 지남
        // 로직: DueDate <= targetDate
        // -------------------------------------------------------
//        LocalDate criteriaDateB = targetDate;

        List<RepaymentSchedule> schedulesB = repaymentScheduleService.findSchedulesByLoanStatusesAndRepaymentStatusAndDueDate(
                targetLoanStatuses,
                RepaymentStatus.PENDING,
                targetDate
        );

        for (RepaymentSchedule schedule : schedulesB) {
            repaymentStatusService.changeRepaymentStatusToMerged(schedule, targetDate);
        }
    }

    private void mergeBalanceForAcceleration(){

        List<RepaymentSchedule>  mergeSchedules = repaymentScheduleService.getRepaymentSchedules(RepaymentStatus.ACCELERATED);

        for(RepaymentSchedule mergeSchedule : mergeSchedules){
            List<RepaymentSchedule> mergedSchedules = repaymentScheduleService.getRepaymentSchedules(mergeSchedule.getLoanAccount().getAccountId(),RepaymentStatus.MERGED);
            repaymentScheduleService.mergeBalance(mergeSchedule,mergedSchedules);
        }
    }

    private void transitionLoanStatus(){
        // 1. 정상화 처리 (Recovery): 다른 상태 -> NORMAL
        transitionStatusToNormal();

        // 2. 연체 진입 (Worsening): NORMAL -> DELINQUENT
        transitionStatusToDelinquent();

        // 3. 기한이익 상실 예고 (Worsening): DELINQUENT -> ACCELERATION_NOTICE
        transitionStatusToAccelerationNotice();

        // 4. 기한이익 상실 확정 (Worsening): ACCELERATION_NOTICE -> ACCELERATION
        transitionStatusToAcceleration();

        // 5. 해지/완제 처리 (Completion): 잔액 0 & 미결 스케줄 없음 -> TERMINATED
        transitionStatusToTerminated();
    }
    private void transitionStatusToNormal() {
        // 1. 후보 검색: 현재 NORMAL이 아니면서 + 비정상 스케줄이 없는 계좌
        List<LoanAccount> candidates = loanAccountService.findCandidateAccountsForStatus(LoanStatus.NORMAL);

        // 2. 상태 변경 적용
        for (LoanAccount loanAccount : candidates) {
            try {
                loanStatusService.changeLoanStatusToNormal(loanAccount);
            } catch (Exception e) {
                // 배치 중 한 건이 실패해도 나머지는 계속 진행 (로그 처리 권장)
                // log.error("Failed to transition to NORMAL for account: " + loanAccount.getAccountId(), e);
            }
        }
    }
    private void transitionStatusToDelinquent() {
        // 1. 후보 검색: 현재 DELINQUENT가 아니면서 + OVERDUE=1, 심각한연체=0 인 계좌
        List<LoanAccount> candidates = loanAccountService.findCandidateAccountsForStatus(LoanStatus.DELINQUENT);

        // 2. 상태 변경 적용
        for (LoanAccount loanAccount : candidates) {
            try {
                loanStatusService.changeLoanStatusToDelinquent(loanAccount);
            } catch (Exception e) {
                // log.error(...)
            }
        }
    }
    private void transitionStatusToAccelerationNotice() {
        // 1. 후보 검색: 현재 NOTICE가 아니면서 + CRITICAL=1, OVERDUE=1 인 계좌
        List<LoanAccount> candidates = loanAccountService.findCandidateAccountsForStatus(LoanStatus.ACCELERATION_NOTICE);

        // 2. 상태 변경 적용
        for (LoanAccount loanAccount : candidates) {
            try {
                loanStatusService.changeLoanStatusToAccelerationNotice(loanAccount);
            } catch (Exception e) {
                // log.error(...)
            }
        }
    }
    private void transitionStatusToAcceleration() {
        // 1. 후보 검색: 현재 ACCELERATION이 아니면서 + ACCELERATED=1 인 계좌
        List<LoanAccount> candidates = loanAccountService.findCandidateAccountsForStatus(LoanStatus.ACCELERATION);

        // 2. 상태 변경 적용
        for (LoanAccount loanAccount : candidates) {
            try {
                loanStatusService.changeLoanStatusToAcceleration(loanAccount);
            } catch (Exception e) {
                // log.error(...)
            }
        }
    }
    private void transitionStatusToTerminated() {
        // 1. 후보 검색: 현재 TERMINATED가 아니면서 + 잔액=0 + 미결스케줄=0 인 계좌
        List<LoanAccount> candidates = loanAccountService.findCandidateAccountsForStatus(LoanStatus.TERMINATED);

        // 2. 상태 변경 적용
        for (LoanAccount loanAccount : candidates) {
            try {
                loanStatusService.changeLoanStatusToTerminated(loanAccount);
            } catch (Exception e) {
                // log.error(...)
            }
        }
    }

    private void updateOverdueBalances() {
        // 모든 요소에 대해 검색 후 처리
        updateBalanceForDelinquent();         // 2. DELINQUENT 상태 처리
        updateBalanceForAccelerationNotice(); // 3. ACC_NOTICE 상태 처리
        updateBalanceForAcceleration();       // 4. ACCELERATION 상태 처리
    }
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












}
