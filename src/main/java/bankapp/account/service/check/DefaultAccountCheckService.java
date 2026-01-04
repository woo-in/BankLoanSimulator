package bankapp.account.service.check;

import bankapp.account.exceptions.AccountNotFoundException;
import bankapp.account.exceptions.PrimaryAccountNotFoundException;
import bankapp.account.model.account.Account;
import bankapp.account.model.account.PrimaryAccount;
import bankapp.account.repository.AccountRepository;
import bankapp.account.repository.PrimaryAccountRepository;
import bankapp.member.model.Member;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static bankapp.core.common.BankCode.WOOIN_BANK;


/**
 * {@inheritDoc}
 * <p>
 * 이 구현체는 AccountDao를 사용하여 데이터베이스에 직접 조회함으로써
 * 계좌 정보 확인 및 조회 로직을 수행합니다.
 */

@Service
@Transactional(readOnly = true)
public class DefaultAccountCheckService implements AccountCheckService {

    private final AccountRepository accountRepository;
    private final PrimaryAccountRepository primaryAccountRepository;

    @Autowired
    public DefaultAccountCheckService(AccountRepository accountRepository , PrimaryAccountRepository primaryAccountRepository){
        this.accountRepository = accountRepository;
        this.primaryAccountRepository = primaryAccountRepository;
    }


    @Override
    public boolean isAccountNumberExist(String accountNumber){

        if (isInvalidAccountNumber(accountNumber)) {
            return false;
        }
        return accountRepository.existsByAccountNumber(accountNumber);
    }


    @Override
    public PrimaryAccount findPrimaryAccountByMember(Member member) throws PrimaryAccountNotFoundException{

        return primaryAccountRepository.findByMember(member)
                .orElseThrow(() -> new PrimaryAccountNotFoundException("주계좌(PRIMARY)를 찾을 수 없습니다"));

    }


    @Override
    public PrimaryAccount findPrimaryAccountByAccountId(Long accountId) throws PrimaryAccountNotFoundException{
        return primaryAccountRepository.findById(accountId)
                .orElseThrow(() -> new PrimaryAccountNotFoundException("해당 회원의 주계좌(PRIMARY)를 찾을 수 없습니다. memberId: " + accountId));
    }


    @Override
    public Account findAccountByAccountNumber(String accountNumber) throws AccountNotFoundException{

        if (isInvalidAccountNumber(accountNumber)) {
            throw new AccountNotFoundException("해당 하는 계좌를 찾을 수 없습니다.");
        }

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("해당 계좌를 찾을 수 없습니다. 계좌번호: " + accountNumber));
    }

    @Override
    public Account findAccountByAccountId(Long accountId) throws AccountNotFoundException{

        if(isInvalidAccountId(accountId)) {
            throw new AccountNotFoundException("해당 하는 계좌를 찾을 수 없습니다.");
        }

        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("해당 계좌를 찾을 수 없습니다. 계좌번호: " + accountId));
    }

    @Override
    public boolean isExternalBank(String bankCode) {
        return !WOOIN_BANK.equals(bankCode);
    }

    private boolean isInvalidAccountNumber(String accountNumber) {
        return accountNumber == null || accountNumber.trim().isEmpty();
    }

    private boolean isInvalidAccountId(Long accountId) {
        return accountId == null ;
    }


    @Override
    public List<Account> findDepositAccountsByMember(Member member) {
        List<Account> allAccounts = accountRepository.findAllByMember(member);

        return allAccounts.stream()
                // 엔티티의 메서드를 호출하여 필터링 (높은 응집도)
                .filter(Account::isAvailableForTransaction)
                .toList();
    }

}
