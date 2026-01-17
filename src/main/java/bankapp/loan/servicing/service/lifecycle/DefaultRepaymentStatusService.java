package bankapp.loan.servicing.service.lifecycle;

import bankapp.loan.exceptions.InvalidRepaymentScheduleException;
import bankapp.loan.exceptions.InvalidRepaymentStatus;
import bankapp.loan.exceptions.InvalidRepaymentStatusException;
import bankapp.loan.servicing.model.LoanStatus;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;
import bankapp.loan.servicing.service.core.RepaymentScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class DefaultRepaymentStatusService implements RepaymentStatusService {


    // DefaultLoanAgingService 와만 공유
    private static final int DAYS_BEFORE_FOR_PENDING = 5;
    private static final int MONTHS_AFTER_FOR_CRITICAL_OVERDUE = 1;
    private static final int MONTHS_AFTER_FOR_ACCELERATED = 2;
    private static final int MONTHS_AFTER_FOR_MERGED = 1;

    private final RepaymentScheduleService repaymentScheduleService;

    @Autowired
    public DefaultRepaymentStatusService(RepaymentScheduleService repaymentScheduleService){
        this.repaymentScheduleService = repaymentScheduleService;
    }


    @Override
    @Transactional
    public void changeRepaymentStatusToComplete(RepaymentSchedule schedule) {
        if (schedule.getStatus() == RepaymentStatus.PLANNED || schedule.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new InvalidRepaymentStatus("can not convert to COMPLETE status.");
        }
        repaymentScheduleService.updateRepaymentStatus(schedule, RepaymentStatus.COMPLETE);
    }

    @Override
    @Transactional
    public void changeRepaymentStatusToPending(RepaymentSchedule schedule, LocalDate targetDate) {
        // 1. RepaymentStatus 조건 검사 (PLANNED 상태여야 함)
        boolean isValidRepaymentStatus = (schedule.getStatus() == RepaymentStatus.PLANNED);

        // 2. LoanStatus 조건 검사 (NORMAL, DELINQUENT, ACCELERATION_NOTICE 중 하나)
        LoanStatus currentLoanStatus = schedule.getLoanAccount().getLoanStatus();
        boolean isValidLoanStatus = (currentLoanStatus == LoanStatus.NORMAL
                || currentLoanStatus == LoanStatus.DELINQUENT
                || currentLoanStatus == LoanStatus.ACCELERATION_NOTICE);

        // 3. 날짜 조건 검사
        boolean isValidDate = !targetDate.isBefore(schedule.getDueDate().minusDays(DAYS_BEFORE_FOR_PENDING));

        // 4. 종합 판단 (모든 조건을 만족해야 실행)
        if (isValidRepaymentStatus && isValidLoanStatus && isValidDate) {
            repaymentScheduleService.updateRepaymentStatus(schedule, RepaymentStatus.PENDING);
        }
    }

    @Override
    @Transactional
    public void changeRepaymentStatusToOverdue(RepaymentSchedule schedule, LocalDate targetDate) {
        // 1. RepaymentStatus 조건 검사 (PENDING 상태여야 함)
        boolean isValidRepaymentStatus = (schedule.getStatus() == RepaymentStatus.PENDING);

        // 2. LoanStatus 조건 검사 (NORMAL, DELINQUENT,중 하나)
        LoanStatus currentLoanStatus = schedule.getLoanAccount().getLoanStatus();
        boolean isValidLoanStatus = (currentLoanStatus == LoanStatus.NORMAL
                || currentLoanStatus == LoanStatus.DELINQUENT);

        // 3. 날짜 조건 검사
        boolean isValidDate = !targetDate.isBefore(schedule.getDueDate());

        // 4. 종합 판단 (모든 조건을 만족해야 실행)
        if (isValidRepaymentStatus && isValidLoanStatus && isValidDate) {
            repaymentScheduleService.updateRepaymentStatus(schedule, RepaymentStatus.OVERDUE);
        }
    }

    @Override
    @Transactional
    public void changeRepaymentStatusToCriticalOverdue(RepaymentSchedule schedule, LocalDate targetDate) {
        // 1. RepaymentStatus 조건 검사 (PENDING 상태여야 함)
        boolean isValidRepaymentStatus = (schedule.getStatus() == RepaymentStatus.OVERDUE);

        // 2. LoanStatus 조건 검사 (DELINQUENT)
        LoanStatus currentLoanStatus = schedule.getLoanAccount().getLoanStatus();
        boolean isValidLoanStatus = (currentLoanStatus == LoanStatus.DELINQUENT);

        // 3. 날짜 조건 검사
        boolean isValidDate = !targetDate.isBefore(schedule.getDueDate().plusMonths(MONTHS_AFTER_FOR_CRITICAL_OVERDUE));

        // 4. 종합 판단 (모든 조건을 만족해야 실행)
        if (isValidRepaymentStatus && isValidLoanStatus && isValidDate) {
            repaymentScheduleService.updateRepaymentStatus(schedule, RepaymentStatus.CRITICAL_OVERDUE);
        }
    }

    @Override
    @Transactional
    public void changeRepaymentStatusToAccelerated(RepaymentSchedule schedule, LocalDate targetDate) {
        // 1. RepaymentStatus 조건 검사 (PENDING 상태여야 함)
        boolean isValidRepaymentStatus = (schedule.getStatus() == RepaymentStatus.CRITICAL_OVERDUE);

        // 2. LoanStatus 조건 검사
        LoanStatus currentLoanStatus = schedule.getLoanAccount().getLoanStatus();
        boolean isValidLoanStatus = (currentLoanStatus == LoanStatus.ACCELERATION_NOTICE);

        // 3. 날짜 조건 검사
        boolean isValidDate = !targetDate.isBefore(schedule.getDueDate().plusMonths(MONTHS_AFTER_FOR_ACCELERATED));

        // 4. 종합 판단 (모든 조건을 만족해야 실행)
        if (isValidRepaymentStatus && isValidLoanStatus && isValidDate) {
            repaymentScheduleService.updateRepaymentStatus(schedule, RepaymentStatus.ACCELERATED);
        }
    }

    @Override
    @Transactional
    public void changeRepaymentStatusToMerged(RepaymentSchedule schedule, LocalDate targetDate) {

        // 공통 정보 조회
        LoanStatus currentLoanStatus = schedule.getLoanAccount().getLoanStatus();
        RepaymentStatus currentRepaymentStatus = schedule.getStatus();
        LocalDate dueDate = schedule.getDueDate();

        // -------------------------------------------------------
        // [조건 A] : 이미 연체(OVERDUE) 상태였고, 특정 기간(Month)이 지난 경우
        // -------------------------------------------------------
        boolean isConditionA =
                (currentRepaymentStatus == RepaymentStatus.OVERDUE)
                        && (currentLoanStatus == LoanStatus.ACCELERATION_NOTICE)
                        && (!targetDate.isBefore(dueDate.plusMonths(MONTHS_AFTER_FOR_MERGED)));

        // -------------------------------------------------------
        // [조건 B] : 임박(PENDING) 상태였는데, 만기일(DueDate)을 지난 경우
        // -------------------------------------------------------
        boolean isConditionB =
                (currentRepaymentStatus == RepaymentStatus.PENDING)
                        && (currentLoanStatus == LoanStatus.ACCELERATION_NOTICE)
                        && (!targetDate.isBefore(dueDate));

        // -------------------------------------------------------
        // 4. 종합 판단 (A 또는 B 중 하나라도 만족하면 실행)
        // -------------------------------------------------------
        if (isConditionA || isConditionB) {
            repaymentScheduleService.updateRepaymentStatus(schedule, RepaymentStatus.MERGED);
        }
    }





}
