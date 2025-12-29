package bankapp.loan.origination.repository;

import bankapp.loan.underwriting.model.LoanContract;
import bankapp.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanContractRepository extends JpaRepository<LoanContract, Long> {
    List<LoanContract> findAllByMember(Member member);
}

