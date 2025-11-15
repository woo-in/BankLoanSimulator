package bankapp.loan.repository.common.application;

import bankapp.loan.model.common.application.ApplicationStatus;
import bankapp.loan.model.common.application.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByApplicationStatusOrderByCreatedAtDesc(ApplicationStatus applicationStatus);
}
