package bankapp.loan.servicing.service.core;

import bankapp.loan.exceptions.*;
import bankapp.loan.servicing.model.LoanAccount;
import bankapp.loan.servicing.model.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 대출 계좌 관리 서비스 인터페이스
 * <p>
 * 대출 계좌의 조회, 잔액 변경, 상태 변경 및 이력 관리,
 * 회차 진행 등의 핵심 비즈니스 로직을 정의합니다.
 */
public interface LoanAccountService {

    /**
     * 대출 계좌 엔티티를 조회합니다.
     *
     * @param loanAccountId 조회할 대출 계좌 ID
     * @return 조회된 대출 계좌 엔티티 (LoanAccount)
     * @throws InvalidLoanAccountException 해당 ID의 계좌를 찾을 수 없는 경우
     */
    LoanAccount getLoanAccount(Long loanAccountId) throws InvalidLoanAccountException;

    /**
     * 대출 계좌의 잔액을 변경(증가/감소)합니다.
     *
     * @param loanAccount 대출 계좌 엔티티
     * @param amount      변경할 금액 (양수: 잔액 증가, 음수: 잔액 감소)
     * @return 변경된 후의 잔액
     * @throws OverRepaymentException 변경 후 잔액이 0보다 작아지는 경우
     */
    BigDecimal updateBalance(LoanAccount loanAccount, BigDecimal amount) throws OverRepaymentException;

    /**
     * 대출 계좌의 상태를 변경합니다.
     * <p>
     * 현재 상태와 목표 상태가 같은 경우 변경하지 않으며,
     * 상태 전이 규칙(Validation)을 통과해야 변경됩니다.
     * </p>
     *
     * @param loanAccount  대출 계좌 엔티티
     * @param targetStatus 변경할 목표 상태 (LoanStatus)
     * @return 상태가 변경된 LoanAccount 엔티티
     * @throws IllegalStateException 유효하지 않은 상태 변경 요청인 경우 (예: 이미 종결된 계좌 변경 시도)
     */
    LoanAccount updateLoanStatus(LoanAccount loanAccount, LoanStatus targetStatus);

    /**
     * 대출의 진행 상황을 업데이트합니다. (회차 증가 및 잔여 원금 갱신)
     * <p>
     * 1. 현재 회차(Installment)를 1 증가시킵니다.<br>
     * 2. 전달받은 잔여 원금으로 엔티티를 갱신합니다.
     * </p>
     *
     * @param loanAccount             대출 계좌 엔티티
     * @param newOutstandingPrincipal 갱신할 잔여 원금
     * @return 갱신된 LoanAccount 엔티티
     * @throws InvalidPrincipalException       잔여 원금이 음수인 경우
     * @throws ActiveLoanContractNotFoundException 활성 상태의 대출 계약을 찾을 수 없는 경우
     * @throws InvalidInstallmentException     다음 회차가 대출 계약 기간을 초과하는 경우
     */
    LoanAccount updateLoanProgress(LoanAccount loanAccount, BigDecimal newOutstandingPrincipal) throws InvalidPrincipalException, ActiveLoanContractNotFoundException, InvalidInstallmentException;


    /**
     * 특정 상태(targetStatus)로 변경되어야 할 조건을 만족하는 대출 계좌 후보 목록을 조회합니다.
     * <p>
     * 조건:
     * 1. 해당 상태가 요구하는 스케줄 조건(RepaymentSchedule Status)을 충족해야 합니다.
     * 2. 현재 이미 해당 상태(targetStatus)인 계좌는 제외됩니다.
     * </p>
     *
     * @param targetStatus 변경 목표 상태 (예: DELINQUENT, NORMAL 등)
     * @return 상태 변경 대상 후보 계좌 리스트 (없으면 빈 리스트 반환)
     */
    List<LoanAccount> findCandidateAccountsForStatus(LoanStatus targetStatus);


    /**
     * 대출 상태 변경 이력을 등록합니다.
     * <p>
     * 1. 기존에 열려있는(Active) 이력이 있다면 종료일(endDate)을 설정하여 마감합니다.<br>
     * 2. 새로운 상태에 대한 이력을 시작일(startDate)과 함께 생성합니다.<br>
     * 3. 대출 계좌(Master)의 현재 상태를 동기화합니다.
     * </p>
     *
     * @param loanAccount   대출 계좌 엔티티
     * @param newStatus     새로 변경될 대출 상태
     * @param effectiveDate 효력 발생 일시 (이력 시작일 및 이전 이력 종료일)
     */
    void registerStatusHistory(LoanAccount loanAccount, LoanStatus newStatus, LocalDateTime effectiveDate);

    /**
     * 현재 진행 중인 대출 상태 이력을 마감합니다.
     * <p>
     * 새로운 이력을 생성하지 않고, 기존에 열려있는 이력의 종료일(endDate)만 업데이트합니다.
     * 주로 대출 완제나 해지 등으로 인해 더 이상 상태 추적이 필요 없거나 종료되는 시점에 사용됩니다.
     * </p>
     *
     * @param loanAccount   대출 계좌 엔티티
     * @param effectiveDate 효력 발생 일시 (이력 종료일)
     */
    void closeStatusHistory(LoanAccount loanAccount, LocalDateTime effectiveDate);
}