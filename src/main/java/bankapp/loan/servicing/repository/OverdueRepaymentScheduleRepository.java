package bankapp.loan.servicing.repository;

import bankapp.loan.servicing.model.OverdueRepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OverdueRepaymentScheduleRepository extends JpaRepository<OverdueRepaymentSchedule, Long> {
}
