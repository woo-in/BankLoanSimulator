package bankapp.loan.servicing.service;

import bankapp.loan.exceptions.ActiveLoanContractNotFoundException;
import bankapp.loan.servicing.dto.RepaymentAllocationInfo;
import bankapp.loan.servicing.model.LoanAccount;

/**
 * 상환 트랜잭션(이력) 관리 서비스 인터페이스
 * <p>
 * 대출 상환이 처리된 후, 그 상세 내역(원금, 이자, 연체료 등 배분 결과)을
 * DB에 영구적인 이력(Transaction History)으로 기록하는 역할을 담당합니다.
 * 주로 자금의 흐름을 추적하거나 회계 처리, 영수증 발급 등의 근거 데이터로 사용됩니다.
 * </p>
 */
public interface RepaymentTransactionService {

    /**
     * 상환 트랜잭션을 기록합니다. (INSERT ONLY)
     * <p>
     * 상환 로직(ScheduleService 등)에서 계산된 배분 정보(Allocation Info)를 바탕으로
     * {@code RepaymentTransaction} 엔티티를 생성하여 저장합니다.
     * </p>
     *
     * @param loanAccount    대출 계좌 엔티티 (이력이 소속될 계좌)
     * @param allocationInfo 상환 처리 결과가 담긴 배분 정보 DTO (원금, 이자, 연체료 등 상세 금액 포함)
     * @throws ActiveLoanContractNotFoundException 활성 상태의 대출 계약을 찾을 수 없는 경우
     */
    void recordRepaymentTransaction(LoanAccount loanAccount, RepaymentAllocationInfo allocationInfo) throws ActiveLoanContractNotFoundException;

}