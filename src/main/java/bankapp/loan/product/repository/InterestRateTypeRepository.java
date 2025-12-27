package bankapp.loan.product.repository;

import bankapp.loan.product.model.InterestRateType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterestRateTypeRepository extends JpaRepository<InterestRateType, Long> {
    Optional<InterestRateType> findByTypeName(String typeName);
}
