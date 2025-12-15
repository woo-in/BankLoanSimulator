package bankapp.loan.servicing.service;


import bankapp.account.model.account.LoanStatus;
import bankapp.loan.servicing.model.RepaymentSchedule;

/**
 * 대출 계좌 상태별 상환 충당 로직을 정의하는 전략 인터페이스.
 */
public interface RepaymentStrategy {

    /**
     * 이 전략이 어떤 LoanStatus에 적용되는지 반환합니다.
     */
    LoanStatus getLoanStatusType();

    /**
     * 특정 스케줄에 대해 상환 및 충당 로직을 수행합니다.
     * 잔액 부족 등 연체 발생 시 DelinquencyService를 호출해야 합니다.
     * * @param schedule 현재 처리할 RepaymentSchedule
     */
    void processRepayment(RepaymentSchedule schedule);
}