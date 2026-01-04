package bankapp.account.repository;

import bankapp.account.model.account.Account;
import bankapp.member.model.Member;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {


    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findByAccountNumber(String accountNumber);


    List<Account> findAllByMember(Member member);


    /**
     * 비관적 쓰기 잠금(PESSIMISTIC_WRITE)을 사용하여 계좌 정보를 조회합니다.
     * 다른 트랜잭션이 해당 레코드에 접근하는 것을 막아 데이터 정합성을 보장합니다.
     * 'javax.persistence.lock.timeout' 힌트는 잠금 대기 시간을 설정합니다 (예: 3초).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    Optional<Account> findById(Long id);
}
