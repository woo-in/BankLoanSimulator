package bankapp.loan.repository.common.repayment;

import bankapp.loan.model.common.repayment.LoanProductRepaymentOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanProductRepaymentOptionRepository extends JpaRepository<LoanProductRepaymentOption, Long> {
}
