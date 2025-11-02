package bankapp.loan.repository.common.rate;

import bankapp.loan.model.common.rate.LoanProductInterestRateTypeOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanProductInterestRateTypeOptionRepository extends JpaRepository<LoanProductInterestRateTypeOption, Long> {
}
