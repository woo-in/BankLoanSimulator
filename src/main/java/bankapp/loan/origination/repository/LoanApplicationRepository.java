package bankapp.loan.origination.repository;

import bankapp.loan.origination.model.ApplicationStatus;
import bankapp.loan.origination.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByApplicationStatusOrderByCreatedAtDesc(ApplicationStatus applicationStatus);
}
