package bankapp.loan.product.repository;

import bankapp.loan.product.model.InterestRateType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRateTypeRepository extends JpaRepository<InterestRateType, Long> {
}
