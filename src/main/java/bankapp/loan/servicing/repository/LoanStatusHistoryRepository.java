package bankapp.loan.servicing.repository;

import bankapp.loan.servicing.model.LoanStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanStatusHistoryRepository extends JpaRepository<LoanStatusHistory, Long> {

}
