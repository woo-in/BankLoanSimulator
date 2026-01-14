package bankapp.loan.servicing.service.lifecycle;

import bankapp.loan.exceptions.InvalidLoanAccountException;
import bankapp.loan.exceptions.InvalidRepaymentScheduleException;
import bankapp.loan.servicing.model.LoanAccount;
import bankapp.loan.servicing.model.LoanStatus;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;
import bankapp.loan.servicing.service.core.LoanAccountService;
import bankapp.loan.servicing.service.core.RepaymentScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// change status 관리 , status history 도 같이 자동으로 해주는 게 맞음
// 대출 상태를 가장 마지막에 바꿀 것
@Service
public class DefaultLoanStatusService implements LoanStatusService {

    private final LoanAccountService loanAccountService;
    private final RepaymentScheduleService repaymentScheduleService;

    @Autowired
    public DefaultLoanStatusService(LoanAccountService loanAccountService,
                                    RepaymentScheduleService repaymentScheduleService) {
        this.loanAccountService = loanAccountService;
        this.repaymentScheduleService = repaymentScheduleService;
    }

    @Transactional
    public void changeLoanStatus(LoanAccount loanAccount, LoanStatus targetStatus) {

        if (loanAccount == null) {
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다.");
        }


        if (targetStatus == LoanStatus.NORMAL) {
            changeLoanStatusToNormal(loanAccount);
        } else if (targetStatus == LoanStatus.DELINQUENT) {
            changeLoanStatusToDelinquent(loanAccount);
        } else if (targetStatus == LoanStatus.ACCELERATION_NOTICE) {
            changeLoanStatusToAccelerationNotice(loanAccount);
        } else if (targetStatus == LoanStatus.ACCELERATION) {
            changeLoanStatusToAcceleration(loanAccount);
        } else if (targetStatus == LoanStatus.TERMINATED) {
            changeLoanStatusToTerminated(loanAccount);
        } else {
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다.");
        }

        loanAccountService.updateLoanStatus(loanAccount, targetStatus);

    }

    private void changeLoanStatusToNormal(LoanAccount loanAccount) {

        // -- normal 조건

        // 대출 원금이 양수
        if (loanAccount.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다(잔여 원금이 있어야 합니다.");
        }

        // 스케줄에 MERGE 0 개 , OVERDUE 0개
        List<RepaymentSchedule> mergedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.MERGED);
        List<RepaymentSchedule> overdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.OVERDUE);

        if (!mergedRepaymentSchedules.isEmpty() || !overdueRepaymentSchedules.isEmpty())
            throw new InvalidRepaymentScheduleException("병합 스케줄 , 연체 스케줄이 존재하지 않아야 delinquent 상태로 바꿀 수 있습니다.");

        // -------------------

        loanAccountService.registerStatusHistory(loanAccount ,LoanStatus.NORMAL ,LocalDateTime.now());
        loanAccountService.updateLoanStatus(loanAccount ,LoanStatus.NORMAL);
    }

    private void changeLoanStatusToDelinquent(LoanAccount loanAccount) {

        // -- delinquent 조건

        // 대출 원금이 양수
        if (loanAccount.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다(잔여 원금이 있어야 합니다.");
        }

        // 스케줄에 MERGE 0 개 , OVERDUE 1개
        List<RepaymentSchedule> mergedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.MERGED);
        List<RepaymentSchedule> overdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.OVERDUE);

        if (!mergedRepaymentSchedules.isEmpty())
            throw new InvalidRepaymentScheduleException("병합 스케줄이 존재하지 않아야 delinquent 상태로 바꿀 수 있습니다.");

        if(overdueRepaymentSchedules.size()!=1) {
            throw new InvalidRepaymentScheduleException("연체 스케줄이 1개 여야 delinquent 상태로 바꿀 수 있습니다.");
        }


        // -------------------

        loanAccountService.registerStatusHistory(loanAccount ,LoanStatus.DELINQUENT ,LocalDateTime.now());
        loanAccountService.updateLoanStatus(loanAccount ,LoanStatus.DELINQUENT);

    }

    private void changeLoanStatusToAccelerationNotice(LoanAccount loanAccount) {
        // -- acceleration_notice 조건

        // 대출 원금이 양수
        if (loanAccount.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다(잔여 원금이 있어야 합니다.");
        }

        // 스케줄에 MERGE 0 개 , OVERDUE 2개
        List<RepaymentSchedule> mergedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.MERGED);
        List<RepaymentSchedule> overdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.OVERDUE);

        if (!mergedRepaymentSchedules.isEmpty())
            throw new InvalidRepaymentScheduleException("병합 스케줄이 존재하지 않아야 acceleration_notice 상태로 바꿀 수 있습니다.");

        if(overdueRepaymentSchedules.size()!=2) {
            throw new InvalidRepaymentScheduleException("연체 스케줄이 2개 여야 acceleration_notice 상태로 바꿀 수 있습니다.");
        }


    // -------------------

        loanAccountService.registerStatusHistory(loanAccount ,LoanStatus.ACCELERATION_NOTICE ,LocalDateTime.now());
        loanAccountService.updateLoanStatus(loanAccount ,LoanStatus.ACCELERATION_NOTICE);
}

    private void changeLoanStatusToAcceleration(LoanAccount loanAccount) {

        // -- acceleration 조건

        // 대출 원금이 양수
        if(loanAccount.getBalance().compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다(잔여 원금이 있어야 합니다.");
        }

        // 스케줄에 PLANNED , PENDING , OVERDUE 없어야 함
        List<RepaymentSchedule> pendingRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId() , RepaymentStatus.PENDING);
        List<RepaymentSchedule> plannedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId() , RepaymentStatus.PLANNED);
        List<RepaymentSchedule> mergedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId() , RepaymentStatus.MERGED);
        List<RepaymentSchedule> overdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId() , RepaymentStatus.OVERDUE);

        if(!pendingRepaymentSchedules.isEmpty() || !plannedRepaymentSchedules.isEmpty() || !overdueRepaymentSchedules.isEmpty()){
            throw new InvalidRepaymentScheduleException("완료되지 않은 대출 스케줄이 있어 상태를 바꿀 수 없습니다.");
        }

        // 스케줄에 MERGED 는 오직 1개 존재
        if(mergedRepaymentSchedules.size() != 1){
            throw new InvalidRepaymentScheduleException("병합 스케줄이 1개 존재해야 acceleration 상태로 바꿀 수 있습니다.");
        }


        // -------------------

        loanAccountService.registerStatusHistory(loanAccount , LoanStatus.ACCELERATION , LocalDateTime.now());
        loanAccountService.updateLoanStatus(loanAccount , LoanStatus.ACCELERATION);


    }

    private void changeLoanStatusToTerminated(LoanAccount loanAccount){
        // -- terminated 조건

        // 상환 원금이 0 이하
        if(loanAccount.getBalance().compareTo(BigDecimal.ZERO) > 0){
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다(잔여 원금이 있습니다.)");
        }


        // 계획 , 대기 , 완료 , 연체 존재 X
        List<RepaymentSchedule> pendingRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId() , RepaymentStatus.PENDING);
        List<RepaymentSchedule> plannedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId() , RepaymentStatus.PLANNED);
        List<RepaymentSchedule> mergedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId() , RepaymentStatus.MERGED);
        List<RepaymentSchedule> overdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId() , RepaymentStatus.OVERDUE);

        if(!pendingRepaymentSchedules.isEmpty() || !plannedRepaymentSchedules.isEmpty() || !mergedRepaymentSchedules.isEmpty() || !overdueRepaymentSchedules.isEmpty()){
            throw new InvalidRepaymentScheduleException("완료되지 않은 대출 스케줄이 있어 상태를 바꿀 수 없습니다.");
        }

        // -------------------

        loanAccountService.closeStatusHistory(loanAccount,LocalDateTime.now());
        loanAccountService.updateLoanStatus(loanAccount , LoanStatus.TERMINATED);
    }
}
