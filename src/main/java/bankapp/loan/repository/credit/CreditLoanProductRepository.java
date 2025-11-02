package bankapp.loan.repository.credit;

import bankapp.loan.model.credit.CreditLoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditLoanProductRepository extends JpaRepository<CreditLoanProduct, Long> {

    Optional<CreditLoanProduct> findByLoanProductSlug(String loanProductSlug);


}
