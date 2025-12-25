package bankapp.loan.origination.repository;

import bankapp.loan.origination.model.ExistingLoan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExistingLoanRepository extends JpaRepository <ExistingLoan, Long>{ }
