package bankapp.account.service.account;

import bankapp.account.exceptions.AccountNotFoundException;
import bankapp.account.exceptions.InsufficientBalanceException;
import bankapp.account.request.account.AccountTransactionRequest;

/**
 * 계좌(Account) 도메인과 관련된 핵심 비즈니스 로직을 처리
 * <p>
 * 이 서비스는 계좌의 상태를 변경(입금, 출금) 하는 공식적인 대표 창구(Facade) 역할을 합니다.
 */
public interface AccountService {

    // TODO: 이 서비스는 돈과 직결된 중요한 부분이니 + , - 를 테이블에 기록하고 이를 일정시간 마다 다른 테이블과 체크하는 백그라운드
    /**
     * 지정된 계좌에서 금액을 출금합니다.
     * <p>
     * 이 메서드는 출금 전, 계좌의 존재 여부와 잔액이 출금할 금액보다 충분한지 검증합니다.
     * 모든 검증을 통과하면 계좌의 잔액을 차감하고 원장(ACCOUNT_LEDGER)에 기록 합니다.
     *
     * @param accountTransactionRequest 요청 정보
     * @throws AccountNotFoundException     해당 accountId의 계좌를 찾을 수 없을 때
     * @throws InsufficientBalanceException 계좌의 잔액이 출금 금액보다 부족할 때
     * @return ledger_id : 원장 기록 id
     */
    long debit(AccountTransactionRequest accountTransactionRequest) throws AccountNotFoundException, InsufficientBalanceException;

    /**
     * 지정된 계좌에 금액을 입금합니다.
     * <p>
     * 이 메서드는 입금 전, 계좌의 존재 여부를 검증합니다.
     * 모든 검증을 통과하면 계좌의 잔액을 차감하고 원장(ACCOUNT_LEDGER)에 기록 합니다.
     *
     * @param accountTransactionRequest 요청 정보
     * @throws AccountNotFoundException 해당 accountId의 계좌를 찾을 수 없을 때
     * @return ledger_id : 원장 기록 id
     */
    long credit(AccountTransactionRequest accountTransactionRequest) throws AccountNotFoundException;




}

