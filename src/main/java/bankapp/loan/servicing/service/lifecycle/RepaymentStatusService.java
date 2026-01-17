package bankapp.loan.servicing.service.lifecycle;

import bankapp.loan.servicing.model.RepaymentSchedule;
import java.time.LocalDate;

/**
 * 상환 스케줄(RepaymentSchedule)의 상태 생명주기(Lifecycle)를 관리하는 서비스 인터페이스입니다.
 * <p>
 * 이 서비스는 날짜 경과, 대출 계좌의 상태(LoanStatus), 그리고 현재 상환 스케줄의 상태(RepaymentStatus)를
 * 종합적으로 판단하여 다음 단계의 상태로 전이(Transition)시키는 역할을 수행합니다.
 * </p>
 */
public interface RepaymentStatusService {

    /**
     * 상환 스케줄의 상태를 <b>상환 완료(COMPLETE)</b>로 변경합니다.
     * <p>
     * <b>전제 조건:</b>
     * <ul>
     * <li>스케줄의 잔액이 0이어야 합니다.</li>
     * <li>현재 상태가 PLANNED이거나 잔액이 남아있는 경우 예외가 발생할 수 있습니다.</li>
     * </ul>
     * </p>
     *
     * @param schedule 상태를 변경할 상환 스케줄 엔티티
     * @throws bankapp.loan.exceptions.InvalidRepaymentStatus 상환 완료 처리할 수 없는 상태인 경우
     */
    void changeRepaymentStatusToComplete(RepaymentSchedule schedule);

    /**
     * 상환 스케줄의 상태를 <b>임박(PENDING)</b>으로 변경합니다.
     * <p>
     * <b>변경 조건 (AND):</b>
     * <ol>
     * <li>현재 상환 상태가 {@code PLANNED}여야 합니다.</li>
     * <li>대출 상태가 {@code NORMAL}, {@code DELINQUENT}, 또는 {@code ACCELERATION_NOTICE} 중 하나여야 합니다.</li>
     * <li>기준 날짜가 만기일(DueDate)의 <b>5일 전</b>을 포함하여 그 이후여야 합니다.</li>
     * </ol>
     * </p>
     *
     * @param schedule 상태를 변경할 상환 스케줄 엔티티
     * @param targetDate 배치 기준 일자 (보통 오늘 날짜)
     */
    void changeRepaymentStatusToPending(RepaymentSchedule schedule, LocalDate targetDate);

    /**
     * 상환 스케줄의 상태를 <b>연체(OVERDUE)</b>로 변경합니다.
     * <p>
     * <b>변경 조건 (AND):</b>
     * <ol>
     * <li>현재 상환 상태가 {@code PENDING}이어야 합니다.</li>
     * <li>대출 상태가 {@code NORMAL} 또는 {@code DELINQUENT}여야 합니다.</li>
     * <li>기준 날짜가 만기일(DueDate)을 지났거나 같아야 합니다.</li>
     * </ol>
     * </p>
     *
     * @param schedule 상태를 변경할 상환 스케줄 엔티티
     * @param targetDate 배치 기준 일자
     */
    void changeRepaymentStatusToOverdue(RepaymentSchedule schedule, LocalDate targetDate);

    /**
     * 상환 스케줄의 상태를 <b>중대 연체(CRITICAL_OVERDUE)</b>로 변경합니다.
     * <p>
     * <b>변경 조건 (AND):</b>
     * <ol>
     * <li>현재 상환 상태가 {@code OVERDUE}여야 합니다.</li>
     * <li>대출 상태가 {@code DELINQUENT}여야 합니다.</li>
     * <li>기준 날짜가 만기일로부터 <b>1개월</b> 이상 경과해야 합니다.</li>
     * </ol>
     * </p>
     *
     * @param schedule 상태를 변경할 상환 스케줄 엔티티
     * @param targetDate 배치 기준 일자
     */
    void changeRepaymentStatusToCriticalOverdue(RepaymentSchedule schedule, LocalDate targetDate);

    /**
     * 상환 스케줄의 상태를 <b>기한이익상실 확정(ACCELERATED)</b>으로 변경합니다.
     * <p>
     * <b>변경 조건 (AND):</b>
     * <ol>
     * <li>현재 상환 상태가 {@code CRITICAL_OVERDUE}여야 합니다.</li>
     * <li>대출 상태가 {@code ACCELERATION_NOTICE} (예정) 상태여야 합니다.</li>
     * <li>기준 날짜가 만기일로부터 <b>2개월</b> 이상 경과해야 합니다.</li>
     * </ol>
     * </p>
     *
     * @param schedule 상태를 변경할 상환 스케줄 엔티티
     * @param targetDate 배치 기준 일자
     */
    void changeRepaymentStatusToAccelerated(RepaymentSchedule schedule, LocalDate targetDate);

    /**
     * 상환 스케줄의 상태를 <b>통합됨(MERGED)</b>으로 변경합니다.
     * <p>
     * 대출이 기한이익상실 예정(ACCELERATION_NOTICE) 상태일 때,
     * 관리가 불필요해진 과거 연체 건이나 도래한 임박 건을 정리하기 위해 사용됩니다.
     * </p>
     * <p>
     * <b>변경 조건 (다음 중 하나 만족 시):</b>
     * <ul>
     * <li><b>조건 A:</b> {@code OVERDUE} 상태이며, 1개월 이상 경과한 경우</li>
     * <li><b>조건 B:</b> {@code PENDING} 상태이며, 만기일이 도래한 경우</li>
     * </ul>
     * (공통 전제: 대출 상태는 {@code ACCELERATION_NOTICE}여야 함)
     * </p>
     *
     * @param schedule 상태를 변경할 상환 스케줄 엔티티
     * @param targetDate 배치 기준 일자
     */
    void changeRepaymentStatusToMerged(RepaymentSchedule schedule, LocalDate targetDate);

}