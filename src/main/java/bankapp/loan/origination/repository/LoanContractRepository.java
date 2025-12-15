package bankapp.loan.origination.repository;

import bankapp.loan.origination.model.LoanContract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanContractRepository extends JpaRepository<LoanContract, Long> { }
