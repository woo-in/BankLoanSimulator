package bankapp.loan.product.repository;

import bankapp.loan.product.model.RepaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepaymentMethodRepository extends JpaRepository<RepaymentMethod, Long> {
}
