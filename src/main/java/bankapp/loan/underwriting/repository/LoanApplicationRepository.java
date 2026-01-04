package bankapp.loan.underwriting.repository;

import bankapp.loan.underwriting.model.ApplicationStatus;
import bankapp.loan.underwriting.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByApplicationStatusOrderByCreatedAtDesc(ApplicationStatus applicationStatus);
    List<LoanApplication> findAllByMember_MemberIdOrderByCreatedAtDesc(Long memberId);
    Optional<LoanApplication> findByLoanApplicationIdAndApplicationStatus(Long loanApplicationId, ApplicationStatus applicationStatus);
}
