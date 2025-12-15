package bankapp.loan.product.repository;

import bankapp.loan.product.model.LoanProductInterestRateTypeOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanProductInterestRateTypeOptionRepository extends JpaRepository<LoanProductInterestRateTypeOption, Long> {
}
