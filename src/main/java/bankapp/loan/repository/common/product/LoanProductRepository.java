package bankapp.loan.repository.common.product;

import bankapp.loan.model.common.product.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {
}
