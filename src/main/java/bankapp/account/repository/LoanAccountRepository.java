package bankapp.account.repository;

import bankapp.loan.servicing.model.LoanAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {


    // 비관적 락(쓰기 락)을 걸어 데이터를 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoanAccount l where l.id = :id")
    Optional<LoanAccount> findByIdWithLock(@Param("id") Long id);

}
