package bankapp.loan.servicing.service;

import bankapp.loan.exceptions.InvalidRepaymentStatusException;
import bankapp.loan.servicing.dto.RepaymentAllocationInfo;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;
import java.math.BigDecimal;
import java.util.List;

/**
 * 상환 스케줄 관리 서비스 인터페이스
 * <p>
 * 대출 상환 스케줄의 조회, 상태 변경, 상환금 배분(충당),
 * 그리고 연체료 및 위약금 계산과 같은 스케줄 관련 핵심 로직을 정의합니다.
 */
public interface RepaymentScheduleService {

    /**
     * 특정 대출 계좌의 상환 스케줄 목록을 조회합니다.
     * <p>
     * 상환 예정일(Due Date) 오름차순으로 정렬하여 반환합니다.
     * </p>
     *
     * @param loanAccountId 대출 계좌 ID
     * @param status        조회할 스케줄 상태 (예: PENDING, OVERDUE 등)
     * @return 조건에 맞는 상환 스케줄 리스트
     * @throws InvalidRepaymentStatusException 상태 정보가 null인 경우
     */
    List<RepaymentSchedule> getRepaymentSchedules(Long loanAccountId, RepaymentStatus status) throws InvalidRepaymentStatusException;

    /**
     * 상환 스케줄의 상태를 변경합니다.
     * <p>
     * 현재 상태와 목표 상태가 같으면 변경하지 않으며,
     * 상태 전이 규칙(Validation)을 통과해야 변경됩니다.
     * (예: 완료된 스케줄을 다시 대기 상태로 되돌릴 수 없음)
     * </p>
     *
     * @param schedule     대상 상환 스케줄 엔티티
     * @param targetStatus 변경할 목표 상태
     * @throws IllegalStateException 유효하지 않은 상태 전이 요청인 경우
     */
    void updateRepaymentStatus(RepaymentSchedule schedule, RepaymentStatus targetStatus);

    /**
     * 상환금을 스케줄에 충당(Allocation)합니다.
     * <p>
     * <b>Waterfall 방식 적용:</b><br>
     * 1. 기한이익 상실 위약금 (Acceleration Penalty)<br>
     * 2. 연체 이자 (Delinquent Amount)<br>
     * 3. 정상 이자 (Interest Amount)<br>
     * 4. 원금 (Principal Amount)<br>
     * 순서로 금액을 차감합니다.
     * </p>
     * <p>
     * 모든 항목이 상환되어 잔액이 0이 되면 스케줄 상태를 COMPLETE로 변경합니다.
     * </p>
     *
     * @param schedule      상환 처리할 스케줄 엔티티
     * @param paymentAmount 사용 가능한 상환 금액
     * @return 이번 처리에서 각 항목별로 얼마나 상환되었는지 담은 상세 내역(DTO)
     */
    RepaymentAllocationInfo applyPaymentToSchedule(RepaymentSchedule schedule, BigDecimal paymentAmount);

    /**
     * 1일치 단순 연체 이자(지연 배상금)를 계산하여 업데이트합니다.
     * <p>
     * 상환 방식에 따른 기준 금액(원금 or 이자)에 연체 가산 금리를 적용하여 계산하며,
     * 계산된 금액은 기존 연체 이자(Delinquent Amount)에 누적됩니다.
     * </p>
     *
     * @param schedule 연체 이자를 계산할 스케줄 엔티티
     * @throws InvalidRepaymentStatusException 지원하지 않는 상환 방식일 경우
     */
    void updateDailyDelinquent(RepaymentSchedule schedule) throws InvalidRepaymentStatusException;

    /**
     * 1일치 기한이익 상실 위약금(Acceleration Penalty)을 계산하여 업데이트합니다.
     * <p>
     * 전체 대출 잔액(Balance)을 기준으로 연체 가산 금리를 적용하여 계산하며,
     * 계산된 금액은 기존 위약금(Acceleration Penalty Amount)에 누적됩니다.
     * 주로 EOD(End of Day) 배치 작업 등에서 호출됩니다.
     * </p>
     *
     * @param schedule 위약금을 계산할 스케줄 엔티티
     */
    void updateDailyAcceleration(RepaymentSchedule schedule);

}