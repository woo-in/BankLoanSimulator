package bankapp.account.service.check;

import bankapp.account.exceptions.AccountNotFoundException;
import bankapp.account.exceptions.PrimaryAccountNotFoundException;
import bankapp.account.model.account.Account;
import bankapp.account.model.account.PrimaryAccount;
import bankapp.member.model.Member;

import java.util.List;

/**
 * 계좌 정보의 존재 여부를 확인하거나, 특정 조건에 맞는 계좌를 조회하는 비즈니스 로직 명세를 정의합니다.
 * 주로 다른 서비스에서 유효성 검증 목적으로 사용됩니다.
 */
public interface AccountCheckService {

    /**
     * 주어진 계좌번호가 실제 데이터베이스에 존재하는지 확인합니다.
     *
     * @param accountNumber 존재 여부를 확인할 계좌번호
     * @return 계좌가 존재하면 true, 그렇지 않으면 false를 반환합니다.
     */
    boolean isAccountNumberExist(String accountNumber);


    /**
     * 주어진 회원에 해당하는 주계좌(PRIMARY) 정보를 조회합니다.
     *
     * @param member 주계좌를 찾을 회원
     * @return 회원의 주계좌 엔티티
     * @throws PrimaryAccountNotFoundException 해당 회원의 주계좌를 찾을 수 없을 경우
     */
    PrimaryAccount findPrimaryAccountByMember(Member member) throws PrimaryAccountNotFoundException;


    /**
     * 주어진 계좌 ID에 해당하는 주계좌(PRIMARY) 정보를 조회합니다.
     *
     * @param accountId 주계좌를 찾을 계좌 고유 ID
     * @return 회원의 주계좌 엔티티
     * @throws PrimaryAccountNotFoundException 해당 회원의 주계좌를 찾을 수 없을 경우
     */
    PrimaryAccount findPrimaryAccountByAccountId(Long accountId) throws PrimaryAccountNotFoundException;


    /**
     * 주어진 계좌번호에 해당하는 계좌 정보를 조회합니다.
     *
     * @param accountNumber 조회할 계좌번호
     * @return 계좌번호에 해당하는 계좌 엔티티
     * @throws AccountNotFoundException 해당 계좌번호의 계좌를 찾을 수 없을 경우
     */
    Account findAccountByAccountNumber(String accountNumber) throws AccountNotFoundException;

    /**
     * 주어진 계좌 ID에 해당하는 계좌 정보를 조회합니다.
     *
     * @param accountId 조회할 계좌 ID
     * @return 회원의 계좌 엔티티
     * @throws AccountNotFoundException 해당 계좌번호의 계좌를 찾을 수 없을 경우
     */
    Account findAccountByAccountId(Long accountId) throws AccountNotFoundException;


    /**
     * 주어진 은행 코드가 '우인은행'이 아닌 타행인지 확인합니다.
     *
     * @param bankCode 확인할 은행 코드
     * @return 타행이면 true, '우인은행'이면 false를 반환합니다.
     */
    boolean isExternalBank(String bankCode);



    /**
     * 입출금이 가능한 계좌 목록만 조회합니다.
     *
     * @param member 회원
     * @return 입출금 가능한 계좌만 반환 합니다.
     */
    public List<Account> findDepositAccountsByMember(Member member);


}
