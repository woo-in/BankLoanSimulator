package bankapp.loan.product.repository;

import bankapp.loan.product.model.LoanProductRepaymentOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanProductRepaymentOptionRepository extends JpaRepository<LoanProductRepaymentOption, Long> {
}
