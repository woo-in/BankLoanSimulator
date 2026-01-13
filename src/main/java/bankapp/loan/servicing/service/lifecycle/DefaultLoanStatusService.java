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
@Service
public class DefaultLoanStatusService implements LoanStatusService {

    private final LoanAccountService loanAccountService;
    private final RepaymentScheduleService repaymentScheduleService;

    @Autowired
    public DefaultLoanStatusService(LoanAccountService loanAccountService,
                                    RepaymentScheduleService repaymentScheduleService){
        this.loanAccountService = loanAccountService;
        this.repaymentScheduleService = repaymentScheduleService;
    }

    @Transactional
    public void changeLoanStatus(LoanAccount loanAccount , LoanStatus targetStatus){

        if(loanAccount == null){
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다.");
        }

        if(targetStatus == LoanStatus.TERMINATED){
            changeLoanStatusToTerminated(loanAccount);
        }
        else if(targetStatus == LoanStatus.NORMAL){
            changeLoanStatusToNormal(loanAccount);
        }
        else{
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다.");
        }

        loanAccountService.updateLoanStatus(loanAccount , targetStatus);

    }

    private void changeLoanStatusToTerminated(LoanAccount loanAccount){

        // -- terminated 조건

        if(loanAccount.getBalance().compareTo(BigDecimal.ZERO) > 0){
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다(잔여 원금이 있습니다.)");
        }

        List<RepaymentSchedule> pendingRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId() , RepaymentStatus.PENDING);
        List<RepaymentSchedule> plannedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId() , RepaymentStatus.PLANNED);
        List<RepaymentSchedule> mergedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId() , RepaymentStatus.MERGED);

        if(!pendingRepaymentSchedules.isEmpty() || !plannedRepaymentSchedules.isEmpty() || !mergedRepaymentSchedules.isEmpty()){
            throw new InvalidRepaymentScheduleException("완료되지 않은 대출 스케줄이 있어 상태를 바꿀 수 없습니다.");
        }

        // -------------------

        loanAccountService.closeStatusHistory(loanAccount,LocalDateTime.now());
        loanAccountService.updateLoanStatus(loanAccount , LoanStatus.TERMINATED);
    }

    private void changeLoanStatusToNormal(LoanAccount loanAccount) {


        // -- normal 조건

        // 대출 원금이 양수
        if(loanAccount.getBalance().compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidLoanAccountException("대출 계좌가 올바르지 않아 상태를 바꿀 수 없습니다(잔여 원금이 있습니다.)");
        }

        List<RepaymentSchedule> pendingRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId() , RepaymentStatus.PENDING);
        List<RepaymentSchedule> mergedRepaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccount.getAccountId() , RepaymentStatus.MERGED);

        // 스케줄에서 merged 가 없어야 함
        if(!mergedRepaymentSchedules.isEmpty()){
            throw new InvalidRepaymentScheduleException("정상 상태가 아닙니다.");
        }

        // 스케줄에서 pending 이 있다면 , pending 들에서 연체금은 없어야 함
        for(RepaymentSchedule schedule : pendingRepaymentSchedules){
            if(schedule.getDelinquentAmount().compareTo(BigDecimal.ZERO) > 0 || schedule.getAccelerationPenaltyAmount().compareTo(BigDecimal.ZERO) > 0){
                throw new InvalidRepaymentScheduleException("정상 상태가 아닙니다.");
            }
        }


        // -------------------

        loanAccountService.registerStatusHistory(loanAccount , LoanStatus.NORMAL , LocalDateTime.now());
        loanAccountService.updateLoanStatus(loanAccount , LoanStatus.NORMAL);
    }


}
