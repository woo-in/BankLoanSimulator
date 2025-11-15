package bankapp.loan.repository.common.contract;

import bankapp.loan.model.common.contract.LoanContract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanContractRepository extends JpaRepository<LoanContract, Long> {

}
