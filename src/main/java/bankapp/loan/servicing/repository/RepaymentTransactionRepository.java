package bankapp.loan.servicing.repository;

import bankapp.loan.servicing.model.RepaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepaymentTransactionRepository extends JpaRepository<RepaymentTransaction, Long> {
}
