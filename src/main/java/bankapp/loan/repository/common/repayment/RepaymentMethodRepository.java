package bankapp.loan.repository.common.repayment;

import bankapp.loan.model.common.repayment.RepaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepaymentMethodRepository extends JpaRepository<RepaymentMethod, Long> {
}
