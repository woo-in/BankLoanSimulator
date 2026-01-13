package bankapp.loan.servicing.service.core;

import bankapp.loan.servicing.model.LoanAccount;
import bankapp.loan.servicing.model.LoanStatus;
import bankapp.account.repository.LoanAccountRepository;
import bankapp.loan.exceptions.*;
import bankapp.loan.servicing.model.LoanStatusHistory;
import bankapp.loan.servicing.repository.LoanStatusHistoryRepository;
import bankapp.loan.underwriting.model.ContractStatus;
import bankapp.loan.underwriting.model.LoanContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;

@Service
public class DefaultLoanAccountService implements LoanAccountService {

    private final LoanAccountRepository loanAccountRepository;
    private final LoanStatusHistoryRepository loanStatusHistoryRepository;

    @Autowired
    public DefaultLoanAccountService(LoanAccountRepository loanAccountRepository,
                                     LoanStatusHistoryRepository loanStatusHistoryRepository) {
        this.loanAccountRepository = loanAccountRepository;
        this.loanStatusHistoryRepository = loanStatusHistoryRepository;
    }


    /**
     * 대출 계좌 엔티티 조회 (단순 조회용)
     * - 비관적 락 없이 가볍게 조회할 때 사용
     */
    @Transactional(readOnly = true)
    public LoanAccount getLoanAccount(Long loanAccountId) {
        return loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> new InvalidLoanAccountException("해당 대출 계좌를 찾을 수 없습니다. ID: " + loanAccountId));
    }

    /**
     * 대출 계좌 잔액 변경 (가감)
     *
     * @param loanAccount 대출 계좌
     * @param amount 변경할 금액 (양수면 증가, 음수면 감소) OverRepaymentException
     * @return 변경 후 잔액
     */
    @Transactional
    public BigDecimal updateBalance(LoanAccount loanAccount, BigDecimal amount) {

        BigDecimal currentBalance = loanAccount.getBalance();
        BigDecimal newBalance = currentBalance.add(amount);

         if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
             throw new OverRepaymentException("대출 잔액은 0보다 작을 수 없습니다.");
         }

        loanAccount.setBalance(newBalance);
        return newBalance;
    }

    // todo : 상태 변화는 다른 서비스 로직에게 위임했는데 , 여기 있다면 ?
    /**
     * 대출 계좌 상태 변경
     *
     * @param loanAccount 대출 계좌 ID
     * @param targetStatus  변경할 목표 상태
     * @return 변경된 LoanAccount 엔티티 (이후 로직에서 참조하기 위함)
     */
    @Transactional
    public LoanAccount updateLoanStatus(LoanAccount loanAccount, LoanStatus targetStatus) {
        loanAccount.setLoanStatus(targetStatus);
        return loanAccount;
    }

    /**
     *  대출 상태 업데이트 (회차 증가 및 예상 잔여 원금 갱신)
     *
     * 1. 회차(CurrentInstallmentNumber)는 자동으로 1 증가
     * 2. 예상 잔여 원금(OutstandingPrincipal)은 인자로 받은 값으로 설정
     *
     * @param loanAccount        대출 계좌
     * @param newOutstandingPrincipal 갱신할 잔여 원금
     * @return 갱신된 LoanAccount 엔티티
     * @throws InvalidPrincipalException 잔여 원금이 음수일 경우
     * @throws ActiveLoanContractNotFoundException 활성 계약이 없을 경우
     * @throws InvalidInstallmentException 증가된 회차가 대출 기간을 초과할 경우
     */
    @Transactional
    public LoanAccount updateLoanProgress(LoanAccount loanAccount, BigDecimal newOutstandingPrincipal) {

        // 2. 잔여 원금 유효성 검증 (0보다 작을 수 없음)
        if (newOutstandingPrincipal.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPrincipalException("대출 잔여 원금은 0보다 작을 수 없습니다. 요청 금액: " + newOutstandingPrincipal);
        }

        // 3. 활성 계약 조회 (만기 회차 확인용) - 예외 처리 변경
        LoanContract activeContract = getActiveContract(loanAccount);
        Integer maxLoanTerm = activeContract.getLoanTerm();

        // 4. 다음 회차 계산 및 검증
        Integer currentInstallment = loanAccount.getCurrentInstallmentNumber();
        Integer nextInstallment = currentInstallment + 1;

        if (nextInstallment > maxLoanTerm) {
            throw new InvalidInstallmentException(
                    String.format("다음 회차(%d)가 대출 계약 기간(%d개월)을 초과합니다.", nextInstallment, maxLoanTerm)
            );
        }

        // 5. 데이터 갱신 (Dirty Checking)
        loanAccount.setOutstandingPrincipal(newOutstandingPrincipal);
        loanAccount.setCurrentInstallmentNumber(nextInstallment);

        return loanAccount;
    }

    /**
     * - 신규: 이력 생성
     * - 변경: 기존 이력 닫기 + 신규 이력 생성
     */
    @Transactional
    public void registerStatusHistory(LoanAccount loanAccount, LoanStatus newStatus, LocalDateTime effectiveDate) {

        // 1. 기존 이력이 있다면 닫기 (공통 로직)
        closeActiveHistoryIfPresent(loanAccount, effectiveDate);

        // 2. 새 이력 생성 (Case 1, 2 모두 수행)
        createNewHistory(loanAccount, newStatus, effectiveDate);

        // 3. 마스터 상태 동기화
        loanAccount.setLoanStatus(newStatus);
    }

    /**
     * [Public] Case 3: 이력 마감 (End Only)
     * - 기존 이력을 닫기만 하고, 새로운 이력은 생성하지 않음.
     */
    @Transactional
    public void closeStatusHistory(LoanAccount loanAccount, LocalDateTime effectiveDate) {

        // 1. 기존 이력이 있다면 닫기 (공통 로직)
        closeActiveHistoryIfPresent(loanAccount, effectiveDate);

        // 2. 새 이력 생성 안 함 (Insert 생략)

        // (선택) 마스터 상태는 그대로 두거나, 필요하다면 별도 처리
    }





    /**
     * 기존에 열려있는 이력이 있으면 찾아서 닫는 메서드
     */
    private void closeActiveHistoryIfPresent(LoanAccount loanAccount, LocalDateTime endDate) {
        loanStatusHistoryRepository.findFirstByLoanAccountAndEndDateIsNullOrderByStartDateDesc(loanAccount)
                .ifPresent(activeHistory -> {
                    // 이미 닫힌 날짜보다 이전 날짜로 닫으려는지 등 날짜 유효성 검사 추가 가능
                    activeHistory.setEndDate(endDate);

                    // 기간 계산
                    long days = java.time.Duration.between(activeHistory.getStartDate(), endDate).toDays();
                    activeHistory.setDurationDays((int) days);
                });
    }

    /**
     * 새 이력 생성 메서드
     */
    private void createNewHistory(LoanAccount loanAccount, LoanStatus status, LocalDateTime startDate) {
        LoanContract activeContract = getActiveContract(loanAccount); // 기존 메서드 활용

        LoanStatusHistory newHistory = LoanStatusHistory.builder()
                .loanAccount(loanAccount)
                .loanContract(activeContract)
                .loanStatus(status)
                .startDate(startDate)
                .endDate(null)
                .build();

        newHistory.setLoanAccount(loanAccount);
        newHistory.setLoanContract(activeContract);

        loanStatusHistoryRepository.save(newHistory);
    }


    /**
     * 내부 헬퍼 메서드: 활성 상태의 대출 계약 조회
     * IllegalStateException 대신 커스텀 예외 사용
     */
    private LoanContract getActiveContract(LoanAccount loanAccount) {
        return loanAccount.getLoanContracts().stream()
                .filter(c -> c.getContractStatus() == ContractStatus.ACTIVE)
                // 최신 버전 우선 (혹시 모를 데이터 꼬임 방지)
                .max(Comparator.comparingInt(LoanContract::getContractVersion))
                .orElseThrow(() -> new ActiveLoanContractNotFoundException(
                        "활성 상태의 대출 계약(LoanContract)이 존재하지 않습니다. Account ID: " + loanAccount.getAccountId()));
    }

}









