package bankapp.loan.servicing.service.process;

import bankapp.account.request.account.AccountTransactionRequest;
import bankapp.account.service.account.AccountService;
import bankapp.loan.exceptions.InvalidRepaymentScheduleException;
import bankapp.loan.servicing.dto.RepaymentAllocationInfo;
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
import java.util.List;

@Service
public class DefaultRepaymentService implements RepaymentService {

    private final LoanAccountService loanAccountService;
    private final RepaymentScheduleService repaymentScheduleService;
    private final RepaymentTransactionService repaymentTransactionService;
    private final RepaymentStatusService repaymentStatusService;
    private final LoanStatusService loanStatusService;
    private final AccountService accountService;

    @Autowired
    public DefaultRepaymentService(LoanAccountService loanAccountService,
                                   RepaymentScheduleService repaymentScheduleService,
                                   RepaymentTransactionService repaymentTransactionService,
                                   RepaymentStatusService repaymentStatusService,
                                   LoanStatusService loanStatusService,
                                   AccountService accountService) {
        this.loanAccountService = loanAccountService;
        this.repaymentScheduleService = repaymentScheduleService;
        this.repaymentTransactionService = repaymentTransactionService;
        this.repaymentStatusService = repaymentStatusService;
        this.loanStatusService = loanStatusService;
        this.accountService = accountService;
    }




    /**
     * 상환 처리 진입점 (Facade)
     * - 대출 계좌의 현재 상태(Status)를 확인하여 적절한 상환 전략(Method)을 실행합니다.
     */
    @Transactional
    public void processRepayment(Long loanAccountId, BigDecimal repaymentAmount) throws InvalidRepaymentScheduleException{

        // 1. 기본 유효성 검증
        if (repaymentAmount == null || repaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("상환 금액은 0보다 커야 합니다.");
        }

        // 2. 계좌 조회
        LoanAccount loanAccount = loanAccountService.getLoanAccount(loanAccountId);
        LoanStatus currentStatus = loanAccount.getLoanStatus();

        // 3. 상태별 로직 분기 (Strategy Pattern using Switch)
        switch (currentStatus) {
            case NORMAL -> processNormalRepayment(loanAccountId, repaymentAmount);

            case DELINQUENT -> processDelinquentRepayment(loanAccountId, repaymentAmount);

            // 기한이익 상실 예고 (가칭)
            case ACCELERATION_NOTICE -> processAccelerationNoticeRepayment(loanAccountId, repaymentAmount);

            // 기한이익 상실 (가칭)
            case ACCELERATION -> processAccelerationRepayment(loanAccountId, repaymentAmount);

            // 그 외 상환 불가 상태 (심사중, 종결 등)
            default -> throw new IllegalStateException(
                    String.format("현재 상태(%s)에서는 상환 처리를 진행할 수 없습니다.", currentStatus)
            );
        }
    }

    // =========================================================================
    //  Status-Specific Repayment Logic
    // =========================================================================

    // RepaymentStatus 에 따른 LoanStatus 상황

    // NORMAL - PENDING(0~1) / OVERDUE(0) / MERGE(0)
    // DELINQUENT - PENDING(0~1) / OVERDUE(1) / MERGE(0)
    // ACC_NOTICE - PENDING(0~1) / OVERDUE(2) / MERGE(0)
    // ACC - PENDING(0) / OVERDUE(0) / MERGE(1)

    // 정상적인 상환 조건
    // NORMAL - PENDING(1) / OVERDUE(0) / MERGE(0)
    // DELINQUENT - PENDING(0~1) / OVERDUE(1) / MERGE(0)
    // ACC_NOTICE - PENDING(0~1) / OVERDUE(2) / MERGE(0)
    // ACC - PENDING(0) / OVERDUE(0) / MERGE(1)

    // processRepayment 는 단건의 스케줄 상태만 처리한다.
    // 예1) DELINQUENT 인데 , 많이 입금했다고 OVERDUE(1) -> PENDING(1) 까지 처리하지는 않는다.
    // 예2) ACC_NOTICE 인데 , 많이 입금했다고 2개의 OVERDUE 전부 처리하고 NORMAL 로 가지 않는다.

    /**
     * [정상] 상태 상환
     * - 도래한(Pending) 최신 회차에 대해 상환을 진행
     * - 남은 돈은 다음 회차를 미리 갚거나(Prepayment), 예치금으로 처리 (정책에 따름)
     */
    private void processNormalRepayment(Long loanAccountId, BigDecimal repaymentAmount) {

        // todo : 2번조회 ?
        LoanAccount loanAccount = loanAccountService.getLoanAccount(loanAccountId);

        // 정상은 오직 1개만 검색
        List<RepaymentSchedule> repaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccountId , RepaymentStatus.PENDING);
        if(repaymentSchedules.size() != 1){
            throw new InvalidRepaymentScheduleException("상환 시점이 아닙니다.");
        }
        RepaymentSchedule schedule = repaymentSchedules.get(0);


        if(repaymentAmount.compareTo(schedule.getTotalAmount()) < 0){
            // <부분 상환>
            processPartialRepayment(loanAccount, schedule, repaymentAmount);
        }
        else{
            // <전체 상환>
            processFullRepayment(loanAccount, schedule);
        }

        // todo : 대출 계좌 상태가 혹시 종료 상태로 바꿀 수 있는지 체크 (가능 -> 바꾸고 대출 종료 / 불가능 -> 예외 throw)
        loanStatusService.changeLoanStatusToTerminated(loanAccount);
    }

    /**
     * [연체] 상태 상환
     * - 가장 오래된 연체 건부터 순차적으로 상환 (Waterfall)
     * - 연체 건이 모두 해소되면 -> NORMAL 상태로 복귀 로직 필요
     */
    private void processDelinquentRepayment(Long loanAccountId, BigDecimal repaymentAmount) {
        // todo : 2번조회 ?
        LoanAccount loanAccount = loanAccountService.getLoanAccount(loanAccountId);

        // 정상은 오직 1개만 검색
        List<RepaymentSchedule> repaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccountId , RepaymentStatus.OVERDUE);

        if(repaymentSchedules.size() != 1){
            throw new InvalidRepaymentScheduleException("정상적인 상환 상황이 아닙니다.");
        }
        RepaymentSchedule schedule = repaymentSchedules.get(0);

        if(repaymentAmount.compareTo(schedule.getTotalAmount()) < 0){
            // <부분 상환>
            processPartialRepayment(loanAccount, schedule, repaymentAmount);
        }
        else{
            // <전체 상환>
            processFullRepayment(loanAccount, schedule);
            loanStatusService.changeLoanStatusToNormal(loanAccount);
        }

        // todo : 대출 계좌 상태가 혹시 종료 상태로 바꿀 수 있는지 체크 (가능 -> 바꾸고 대출 종료 / 불가능 -> 예외 throw)
        loanStatusService.changeLoanStatusToTerminated(loanAccount);

    }

    /**
     * [기한이익 상실 예고] 상태 상환
     * - 연체 상태와 유사하나, 로깅이나 알림 해제 등의 부가 로직이 다를 수 있음
     * - 연체금 해소 시 상태 복귀 처리 중요
     */
    private void processAccelerationNoticeRepayment(Long loanAccountId, BigDecimal repaymentAmount) {

        // todo : 2번조회 ?
        LoanAccount loanAccount = loanAccountService.getLoanAccount(loanAccountId);

        List<RepaymentSchedule> repaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccountId , RepaymentStatus.OVERDUE);

        if(repaymentSchedules.size() != 2){
            throw new InvalidRepaymentScheduleException("정상적인 상환 상황이 아닙니다.");
        }

        RepaymentSchedule schedule = repaymentSchedules.get(0);

        if(repaymentAmount.compareTo(schedule.getTotalAmount()) < 0){
            // <부분 상환>
            processPartialRepayment(loanAccount, schedule, repaymentAmount);
        }
        else{
            // <전체 상환>
            processFullRepayment(loanAccount, schedule);
            loanStatusService.changeLoanStatusToDelinquent(loanAccount);
        }


        // todo : 대출 계좌 상태가 혹시 종료 상태로 바꿀 수 있는지 체크 (가능 -> 바꾸고 대출 종료 / 불가능 -> 예외 throw)
//        loanStatusService.changeLoanStatus(loanAccount, LoanStatus.TERMINATED);
    }

    /**
     * [기한이익 상실] 상태 상환 (법적 절차 진행 중 등)
     * - 스케줄 개념보다는 '전체 잔액 + 확정된 위약금'을 갚는 개념이 강함
     * - 일부 상환 시에도 상태가 바로 정상으로 돌아오지 않음 (전액 변제 필요할 수 있음)
     */
    private void processAccelerationRepayment(Long loanAccountId, BigDecimal repaymentAmount) {

        // todo : 2번조회 ?
        LoanAccount loanAccount = loanAccountService.getLoanAccount(loanAccountId);

        List<RepaymentSchedule> repaymentSchedules = repaymentScheduleService.getRepaymentSchedules(loanAccountId , RepaymentStatus.MERGED);

        if(repaymentSchedules.size() != 1){
            throw new InvalidRepaymentScheduleException("정상적인 상환 상황이 아닙니다.");
        }

        RepaymentSchedule schedule = repaymentSchedules.get(0);

        if(repaymentAmount.compareTo(schedule.getTotalAmount()) < 0){
            // <부분 상환>
            processPartialRepayment(loanAccount, schedule, repaymentAmount);
        }
        else{
            // <전체 상환>
            processFullRepayment(loanAccount, schedule);
            loanStatusService.changeLoanStatusToTerminated(loanAccount);
        }


    }


    /**
     * 부분 상환 처리
     */
    private void processPartialRepayment(LoanAccount loanAccount, RepaymentSchedule schedule, BigDecimal repaymentAmount) {
        // 부분 상환은 공통 상환 로직만 수행하면 됨
        executeRepaymentCommon(loanAccount, schedule, repaymentAmount);
    }

    /**
     * 전체 상환 처리
     */
    private void processFullRepayment(LoanAccount loanAccount, RepaymentSchedule schedule) {
        // 1. 전체 상환이므로 금액을 스케줄의 남은 총액으로 강제 설정 (초과 입금 방지)
        BigDecimal fullAmount = schedule.getTotalAmount();

        // 2. 공통 상환 로직 수행
        executeRepaymentCommon(loanAccount, schedule, fullAmount);

        // 3. [전체 상환만의 추가 로직] 스케줄 상태를 COMPLETE로 변경
        repaymentStatusService.changeRepaymentStatusToComplete(schedule);

    }

    /**
     * [공통 로직] 실제 상환 트랜잭션 실행
     * - 스케줄 차감, 잔액 갱신, 계좌 이체, 기록 저장을 담당
     */
    private void executeRepaymentCommon(LoanAccount loanAccount, RepaymentSchedule schedule, BigDecimal amount) {
        // 1. repayment_schedule 가감 (Allocation)
        RepaymentAllocationInfo repaymentAllocationInfo =
                repaymentScheduleService.applyPaymentToSchedule(schedule, amount);

        // 2. loan_account 가감 (잔액 업데이트)
        BigDecimal newBalance =
                loanAccountService.updateBalance(loanAccount, repaymentAllocationInfo.getPrincipalAmount());

        // 3. 실제 상환 (계좌 이체)
        // 3-1. 은행(또는 모계좌)로 입금 (Credit)
        AccountTransactionRequest depositRequest = new AccountTransactionRequest(
                1L, // Long.parseLong("1")을 1L로 간소화
                repaymentAllocationInfo.getTotalRepaymentAmount(),
                "상환금 입금"
        );
        accountService.credit(depositRequest);

        // 3-2. 고객 상환 계좌에서 출금 (Debit)
        AccountTransactionRequest withdrawalRequest = new AccountTransactionRequest(
                loanAccount.getRepaymentAccount().getAccountId(),
                repaymentAllocationInfo.getTotalRepaymentAmount(),
                "상환금 출금"
        );
        accountService.debit(withdrawalRequest);

        // 4. loan_repayment_transaction 기록
        repaymentTransactionService.recordRepaymentTransaction(loanAccount, repaymentAllocationInfo);
    }

}