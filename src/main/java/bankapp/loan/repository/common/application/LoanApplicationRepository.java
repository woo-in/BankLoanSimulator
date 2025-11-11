package bankapp.loan.repository.common.application;

import bankapp.loan.model.common.application.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
}
