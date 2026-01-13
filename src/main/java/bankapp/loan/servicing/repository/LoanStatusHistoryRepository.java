package bankapp.loan.servicing.repository;

import bankapp.loan.servicing.model.LoanAccount;
import bankapp.loan.servicing.model.LoanStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanStatusHistoryRepository extends JpaRepository<LoanStatusHistory, Long> {

    // 특정 계좌의 '현재 진행 중인(endDate가 없는)' 이력 조회
    Optional<LoanStatusHistory> findFirstByLoanAccountAndEndDateIsNullOrderByStartDateDesc(LoanAccount loanAccount);

}
