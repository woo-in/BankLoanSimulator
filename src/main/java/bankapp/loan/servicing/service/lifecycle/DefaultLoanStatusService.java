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


    @Override
    @Transactional
    public void changeLoanStatusToNormal(LoanAccount loanAccount) {

        if (loanAccount.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다(잔여 원금이 있어야 합니다.");
        }

//        List<RepaymentSchedule> pendingRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.PENDING);
//        List<RepaymentSchedule> plannedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.PLANNED);
        List<RepaymentSchedule> mergedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.MERGED);
        List<RepaymentSchedule> overdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.OVERDUE);
        List<RepaymentSchedule> criticalOverdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.CRITICAL_OVERDUE);
        List<RepaymentSchedule> acceleratedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.ACCELERATED);

        if (!mergedRepaymentSchedules.isEmpty() || !overdueRepaymentSchedules.isEmpty() || !criticalOverdueRepaymentSchedules.isEmpty() || !acceleratedRepaymentSchedules.isEmpty()) {
            throw new InvalidRepaymentScheduleException("Normal 상태로 바꿀 수 없습니다.");
        }

        loanAccountService.registerStatusHistory(loanAccount ,LoanStatus.NORMAL ,LocalDateTime.now());
        loanAccountService.updateLoanStatus(loanAccount ,LoanStatus.NORMAL);
    }

    @Override
    @Transactional
    public void changeLoanStatusToDelinquent(LoanAccount loanAccount) {

        if (loanAccount.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다(잔여 원금이 있어야 합니다.");
        }

        // List<RepaymentSchedule> pendingRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.PENDING);
        // List<RepaymentSchedule> plannedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.PLANNED);
        List<RepaymentSchedule> mergedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.MERGED);
        List<RepaymentSchedule> overdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.OVERDUE);
        List<RepaymentSchedule> criticalOverdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.CRITICAL_OVERDUE);
        List<RepaymentSchedule> acceleratedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.ACCELERATED);

        if (!mergedRepaymentSchedules.isEmpty() || overdueRepaymentSchedules.size()!=1 || !criticalOverdueRepaymentSchedules.isEmpty() || !acceleratedRepaymentSchedules.isEmpty()) {
            throw new InvalidRepaymentScheduleException("Delinquent 상태로 바꿀 수 없습니다.");
        }

        loanAccountService.registerStatusHistory(loanAccount ,LoanStatus.DELINQUENT ,LocalDateTime.now());
        loanAccountService.updateLoanStatus(loanAccount ,LoanStatus.DELINQUENT);

    }

    @Override
    @Transactional
    public void changeLoanStatusToAccelerationNotice(LoanAccount loanAccount) {

        if (loanAccount.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다(잔여 원금이 있어야 합니다.");
        }

        //        List<RepaymentSchedule> pendingRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.PENDING);
        //        List<RepaymentSchedule> plannedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.PLANNED);
        List<RepaymentSchedule> mergedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.MERGED);
        List<RepaymentSchedule> overdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.OVERDUE);
        List<RepaymentSchedule> criticalOverdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.CRITICAL_OVERDUE);
        List<RepaymentSchedule> acceleratedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.ACCELERATED);


        if (!mergedRepaymentSchedules.isEmpty() || overdueRepaymentSchedules.size()!= 1 || criticalOverdueRepaymentSchedules.size()!=1 || acceleratedRepaymentSchedules.isEmpty()) {
            throw new InvalidRepaymentScheduleException("Delinquent 상태로 바꿀 수 없습니다.");
        }

    // -------------------

        loanAccountService.registerStatusHistory(loanAccount ,LoanStatus.ACCELERATION_NOTICE ,LocalDateTime.now());
        loanAccountService.updateLoanStatus(loanAccount ,LoanStatus.ACCELERATION_NOTICE);
}

    @Override
    @Transactional
    public void changeLoanStatusToAcceleration(LoanAccount loanAccount) {

        if(loanAccount.getBalance().compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다(잔여 원금이 있어야 합니다.");
        }

        List<RepaymentSchedule> pendingRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.PENDING);
        List<RepaymentSchedule> plannedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.PLANNED);
//        List<RepaymentSchedule> mergedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.MERGED);
        List<RepaymentSchedule> overdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.OVERDUE);
        List<RepaymentSchedule> criticalOverdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.CRITICAL_OVERDUE);
        List<RepaymentSchedule> acceleratedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.ACCELERATED);



        if(!pendingRepaymentSchedules.isEmpty() || !plannedRepaymentSchedules.isEmpty() || !overdueRepaymentSchedules.isEmpty() || !criticalOverdueRepaymentSchedules.isEmpty() || acceleratedRepaymentSchedules.size() != 1) {
            throw new InvalidRepaymentScheduleException("완료되지 않은 대출 스케줄이 있어 상태를 바꿀 수 없습니다.");
        }

        loanAccountService.registerStatusHistory(loanAccount , LoanStatus.ACCELERATION , LocalDateTime.now());
        loanAccountService.updateLoanStatus(loanAccount , LoanStatus.ACCELERATION);
    }

    @Override
    @Transactional
    public void changeLoanStatusToTerminated(LoanAccount loanAccount){

        if(loanAccount.getBalance().compareTo(BigDecimal.ZERO) > 0){
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다(잔여 원금이 있습니다.)");
        }

        List<RepaymentSchedule> pendingRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.PENDING);
        List<RepaymentSchedule> plannedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.PLANNED);
//        List<RepaymentSchedule> mergedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.MERGED);
        List<RepaymentSchedule> overdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.OVERDUE);
        List<RepaymentSchedule> criticalOverdueRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.CRITICAL_OVERDUE);
        List<RepaymentSchedule> acceleratedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId(), RepaymentStatus.ACCELERATED);


        if(!pendingRepaymentSchedules.isEmpty() || !plannedRepaymentSchedules.isEmpty() || !overdueRepaymentSchedules.isEmpty() || !criticalOverdueRepaymentSchedules.isEmpty() || !acceleratedRepaymentSchedules.isEmpty()) {
            throw new InvalidRepaymentScheduleException("완료되지 않은 대출 스케줄이 있어 상태를 바꿀 수 없습니다.");
        }

        // -------------------

        loanAccountService.closeStatusHistory(loanAccount,LocalDateTime.now());
        loanAccountService.updateLoanStatus(loanAccount , LoanStatus.TERMINATED);
    }

    // compelete
}
