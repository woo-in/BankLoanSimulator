package bankapp.loan.origination.repository;

import bankapp.loan.origination.model.PendingLoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingLoanApplicationRepository extends JpaRepository <PendingLoanApplication, Long>{ }
