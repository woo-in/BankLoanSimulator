package bankapp.loan.repository.common.rate;

import bankapp.loan.model.common.rate.InterestRateType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRateTypeRepository extends JpaRepository<InterestRateType, Long> {
}
