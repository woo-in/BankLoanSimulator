package bankapp.loan.repository.common.schedule;

import bankapp.loan.model.common.schedule.RepaymentSchedule;
import bankapp.loan.model.common.schedule.RepaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByRepaymentDateAndStatus(LocalDate repaymentDate, RepaymentStatus status);
}
