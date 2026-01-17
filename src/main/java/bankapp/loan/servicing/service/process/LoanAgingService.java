package bankapp.loan.servicing.service.process;

import java.time.LocalDate;

/**
 * 대출 채권의 일일 마감 처리를 담당하는 서비스 인터페이스입니다.
 * <p>
 * 이 서비스는 매일 정해진 시간에 실행되어야 하며, 다음과 같은 순서로 상태와 잔액을 갱신합니다.
 * <ol>
 * <li><b>상환 스케줄(RepaymentSchedule) 상태 전이:</b> 만기 도래 여부에 따라 PENDING, OVERDUE, CRITICAL_OVERDUE 등으로 상태를 변경합니다.</li>
 * <li><b>잔액 병합(Merge):</b> 기한이익 상실(Acceleration) 확정 시, 관련 스케줄들의 잔액을 통합합니다.</li>
 * <li><b>대출 계좌(LoanStatus) 상태 전이:</b> 변경된 스케줄 상태를 기반으로 대출 계좌의 상태(NORMAL, DELINQUENT 등)를 재조정합니다.</li>
 * <li><b>연체 금액 갱신:</b> 연체된 원금에 대한 지연 배상금(연체 이자) 등을 계산하여 잔액에 반영합니다.</li>
 * </ol>
 * </p>
 */
public interface LoanAgingService {

    /**
     * 지정된 기준 날짜(targetDate)를 기준으로 일일 Aging 배치를 실행합니다.
     * <p>
     * 이 메서드는 트랜잭션 단위로 실행되어야 하며, 대출 전체 사이클(RS -> LS -> Balance)의
     * 정합성을 보장하는 순서로 로직을 수행합니다.
     * </p>
     *
     * @param targetDate 배치 기준 일자 (보통 오늘 날짜 또는 마감 영업일)
     */
    void processDailyAging(LocalDate targetDate);

}