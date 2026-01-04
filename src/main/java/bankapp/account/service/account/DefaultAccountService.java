package bankapp.account.service.account;

import bankapp.account.exceptions.AccountNotFoundException;
import bankapp.account.exceptions.InsufficientBalanceException;
import bankapp.account.exceptions.InvalidAmountException;
import bankapp.account.model.account.Account;
import bankapp.account.model.ledger.AccountLedger;
import bankapp.account.model.ledger.TransactionType;
import bankapp.account.repository.AccountLedgerRepository;
import bankapp.account.repository.AccountRepository;
import bankapp.account.request.account.AccountTransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class DefaultAccountService implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountLedgerRepository accountLedgerRepository;


    @Autowired
    public DefaultAccountService(AccountRepository accountRepository , AccountLedgerRepository accountLedgerRepository ){
        this.accountRepository = accountRepository;
        this.accountLedgerRepository = accountLedgerRepository;
    }

    // TODO: 트랜잭션 최적으로 바꾸기
    @Override
    @Transactional
    public long debit(AccountTransactionRequest accountTransactionRequest) throws AccountNotFoundException, InsufficientBalanceException , InvalidAmountException {
        Account account = prepareTransaction(accountTransactionRequest.getAccountId(), accountTransactionRequest.getAmount());

        // 출금에만 필요한 잔액 검증 로직 수행
        validateSufficientBalance(account, accountTransactionRequest.getAmount());

        return applyTransaction(account, accountTransactionRequest, TransactionType.DEBIT).getLedgerId();
    }

    @Override
    @Transactional
    public long credit(AccountTransactionRequest accountTransactionRequest) throws AccountNotFoundException, InsufficientBalanceException , InvalidAmountException{

        Account account = prepareTransaction(accountTransactionRequest.getAccountId(), accountTransactionRequest.getAmount());

        return applyTransaction(account, accountTransactionRequest, TransactionType.CREDIT).getLedgerId();

    }




    /**
     * 요청 DTO 자체의 유효성을 검증합니다. (예: 금액이 양수인지)
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("거래 금액은 0보다 커야 합니다.");
        }
    }

    /**
     * 계좌를 비관적 잠금으로 조회하고, 존재하지 않을 경우 예외를 발생시킵니다.
     */
    private Account lockAndGetAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계좌가 존재하지 않습니다. ID: " + accountId));
    }

    /**
     *  트랜잭션 처리를 위한 공통 준비 작업을 수행합니다.
     * (금액 유효성 검사, 계좌 잠금 및 조회)
     */
    private Account prepareTransaction(Long accountId, BigDecimal amount) {
        validateAmount(amount);
        return lockAndGetAccount(accountId);
    }

    /**
     * 계좌의 잔액이 출금 금액에 충분한지 검증합니다.
     */
    private void validateSufficientBalance(Account account, BigDecimal amountToDebit) {
        if (account.getBalance().compareTo(amountToDebit) < 0) {
            throw new InsufficientBalanceException("계좌 잔액이 부족합니다.");
        }
    }

    /**
     * TransactionType에 따라 잔액을 계산하고 거래를 기록합니다.
     * 이후 AccountLedger 를 반환
     */
    private AccountLedger applyTransaction(Account account, AccountTransactionRequest request, TransactionType type) {
        // TransactionType에 계산 로직을 위임
        BigDecimal balanceAfter = type.calculate(account.getBalance(), request.getAmount());
        account.setBalance(balanceAfter);
        return accountLedgerRepository.save(AccountLedger.from(account,request, type, balanceAfter));
    }

}
