package bankapp.loan.servicing.service;

import bankapp.account.model.account.LoanAccount;
import bankapp.loan.underwriting.model.LoanContract;

import java.time.LocalDate;

public interface LoanRepaymentService {

    /**
     * 최초 대출 계약 체결 시, 전체 대출 기간에 대한 상환 스케줄을 생성하고 저장합니다.
     *
     * <p>
     * <b>상환 시작일 산정 기준:</b><br>
     * 대출 실행일이 속한 달의 다음 달은 거치 기간으로 간주하며,
     * 실질적인 첫 상환은 <b>대출 실행일 기준 다다음 달(익익월)</b>의 납입일(Payment Day)부터 시작됩니다.
     * </p>
     *
     * <p><b>예시:</b></p>
     * <ul>
     * <li>대출 실행일: 12월 31일</li>
     * <li>납입일(Payment Day): 매월 지정일</li>
     * <li>첫 상환 시작: 1월은 건너뛰고 <b>2월</b> 납입일부터 상환 시작</li>
     * </ul>
     *
     * @param loanAccount  생성된 대출 계좌 (대출 기간, 납입일 정보 포함)
     * @param loanContract 체결된 대출 계약 정보 (금리, 상환 방식, 스프레드 등 포함)
     */
    void saveRepaymentSchedule(LoanAccount loanAccount , LoanContract loanContract);


    /**
     * 다가오는 상환 회차에 대해 청구 내역을 확정(Billing)하고, 납부 대기 상태로 전환합니다.
     *
     * <p>
     * 이 메서드는 배치(Batch) 또는 스케줄러에 의해 주기적으로 실행되며,
     * 기준일로부터 특정 기간(Billing Horizon) 내에 만기가 도래하는 <b>상환 예정(PLANNED)</b> 스케줄을 처리합니다.
     * </p>
     *
     * <p><b>주요 처리 로직:</b></p>
     * <ul>
     * <li><b>1. 대상 조회:</b> 기준일(`date`) + 여유일(`daysToAdd`) 이내에 만기가 도래하는 `PLANNED` 상태의 스케줄을 조회합니다.</li>
     * <li><b>2. 변동 금리 적용 (Re-calculation):</b>
     * <ul>
     * <li><b>고정 금리:</b> 기존에 계산된 원금과 이자를 그대로 유지합니다.</li>
     * <li><b>변동 금리:</b> <b>현재 시점의 기준 금리(Base Rate)</b>와 <b>직전 회차까지의 스케줄상 잔액</b>을 기반으로
     * 이번 회차에 납부해야 할 원금과 이자를 재산정합니다. (원리금균등상환의 경우 남은 기간에 대한 재상각 수행)</li>
     * </ul>
     * </li>
     * <li><b>3. 상태 변경:</b> 스케줄 상태를 {@code PLANNED}에서 {@code PENDING}으로 변경하여, 고객이 납부 가능한 상태로 활성화합니다.</li>
     * </ul>
     *
     * <p>
     * <b>주의:</b> 이 과정에서 스케줄의 <b>납부 예정일(Due Date)</b>은 변경되지 않으며,
     * 오직 <b>청구 금액(Interest/Principal Amount)</b>과 <b>상태(Status)</b>만 갱신됩니다.
     * </p>
     *
     * @param date      배치 실행 기준일 (주로 시스템 현재 날짜)
     * @param daysToAdd 청구 확정 기준일수 (예: 5일 전 청구 확정 시 '5' 입력)
     */
    void prepareUpcomingRepaymentSchedule(LocalDate date , int daysToAdd);
}

