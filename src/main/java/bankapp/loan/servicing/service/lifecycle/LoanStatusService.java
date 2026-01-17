package bankapp.loan.servicing.service.lifecycle;

import bankapp.loan.servicing.model.LoanAccount;

/**
 * 대출 계좌의 생명주기(LifeCycle) 상태 변경을 담당하는 서비스 인터페이스입니다.
 * <p>
 * 이 서비스는 대출 계좌의 잔액 및 상환 스케줄(RepaymentSchedule)의 상태를 검증하고,
 * 유효한 경우 대출 상태(LoanStatus)를 변경하며 변경 이력(History)을 기록합니다.
 * </p>
 */
public interface LoanStatusService {

    /**
     * 대출 계좌의 상태를 <b>NORMAL(정상)</b>로 변경합니다.
     * <p>
     * <b>전제 조건:</b>
     * <ul>
     * <li>대출 잔액이 0보다 커야 합니다.</li>
     * <li>병합(Merged), 연체(Overdue), 기한이익상실(Critical Overdue, Accelerated) 상태의 스케줄이 없어야 합니다.</li>
     * </ul>
     * </p>
     *
     * @param loanAccount 상태를 변경할 대출 계좌
     * @throws bankapp.loan.exceptions.InvalidLoanAccountException 잔액이 0 이하인 경우
     * @throws bankapp.loan.exceptions.InvalidRepaymentScheduleException 해소되지 않은 연체나 기한이익상실 스케줄이 존재하는 경우
     */
    void changeLoanStatusToNormal(LoanAccount loanAccount);

    /**
     * 대출 계좌의 상태를 <b>DELINQUENT(연체)</b>로 변경합니다.
     * <p>
     * <b>전제 조건:</b>
     * <ul>
     * <li>대출 잔액이 0보다 커야 합니다.</li>
     * <li>유효한 연체 스케줄이 존재해야 하며, 다른 비정상 스케줄 조건과 충돌하지 않아야 합니다.</li>
     * </ul>
     * </p>
     *
     * @param loanAccount 상태를 변경할 대출 계좌
     * @throws bankapp.loan.exceptions.InvalidLoanAccountException 잔액이 0 이하인 경우
     * @throws bankapp.loan.exceptions.InvalidRepaymentScheduleException 연체 상태로 변경하기 위한 스케줄 조건이 충족되지 않은 경우
     */
    void changeLoanStatusToDelinquent(LoanAccount loanAccount);

    /**
     * 대출 계좌의 상태를 <b>ACCELERATION_NOTICE(기한이익 상실 예고)</b>로 변경합니다.
     * <p>
     * <b>전제 조건:</b>
     * <ul>
     * <li>대출 잔액이 0보다 커야 합니다.</li>
     * <li>연체 및 기한이익 상실 예고에 부합하는 스케줄 상태여야 합니다.</li>
     * </ul>
     * </p>
     *
     * @param loanAccount 상태를 변경할 대출 계좌
     * @throws bankapp.loan.exceptions.InvalidLoanAccountException 잔액이 0 이하인 경우
     * @throws bankapp.loan.exceptions.InvalidRepaymentScheduleException 스케줄 상태가 예고 조건에 부합하지 않는 경우
     */
    void changeLoanStatusToAccelerationNotice(LoanAccount loanAccount);

    /**
     * 대출 계좌의 상태를 <b>ACCELERATION(기한이익 상실 - 조기상환 청구)</b>로 변경합니다.
     * <p>
     * 모든 미상환 원리금에 대해 즉시 상환 의무가 발생하는 단계입니다.
     * <p>
     * <b>전제 조건:</b>
     * <ul>
     * <li>대출 잔액이 0보다 커야 합니다.</li>
     * <li>다른 진행 중인 스케줄 없이, 오직 확정된 기한이익 상실(Accelerated) 스케줄만 존재해야 합니다.</li>
     * </ul>
     * </p>
     *
     * @param loanAccount 상태를 변경할 대출 계좌
     * @throws bankapp.loan.exceptions.InvalidLoanAccountException 잔액이 0 이하인 경우
     * @throws bankapp.loan.exceptions.InvalidRepaymentScheduleException 완료되지 않은 일반/연체 스케줄이 남아있거나, 가속화 스케줄이 유효하지 않은 경우
     */
    void changeLoanStatusToAcceleration(LoanAccount loanAccount);

    /**
     * 대출 계좌의 상태를 <b>TERMINATED(해지/완제)</b>로 변경합니다.
     * <p>
     * 대출이 완전히 상환되어 종료되는 단계입니다.
     * <p>
     * <b>전제 조건:</b>
     * <ul>
     * <li>대출 잔액이 <b>0</b>이어야 합니다.</li>
     * <li>미결(Pending, Planned, Overdue 등) 상태의 잔여 스케줄이 없어야 합니다.</li>
     * </ul>
     * </p>
     *
     * @param loanAccount 상태를 변경할 대출 계좌
     * @throws bankapp.loan.exceptions.InvalidLoanAccountException 잔액이 남아있는 경우
     * @throws bankapp.loan.exceptions.InvalidRepaymentScheduleException 처리가 완료되지 않은 잔여 스케줄이 존재하는 경우
     */
    void changeLoanStatusToTerminated(LoanAccount loanAccount);
}