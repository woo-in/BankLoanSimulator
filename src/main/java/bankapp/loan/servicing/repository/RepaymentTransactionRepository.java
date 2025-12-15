package bankapp.loan.servicing.repository;

import bankapp.loan.servicing.model.LoanRepaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepaymentTransactionRepository extends JpaRepository<LoanRepaymentTransaction, Long> {
}
