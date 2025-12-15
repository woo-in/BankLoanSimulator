package bankapp.loan.product.repository;

import bankapp.loan.product.model.CreditLoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditLoanProductRepository extends JpaRepository<CreditLoanProduct, Long> {

    Optional<CreditLoanProduct> findByLoanProductSlug(String loanProductSlug);


}
