package bankapp.loan.underwriting.repository;

import bankapp.loan.underwriting.model.ApplicationStatus;
import bankapp.loan.underwriting.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByApplicationStatusOrderByCreatedAtDesc(ApplicationStatus applicationStatus);
}
