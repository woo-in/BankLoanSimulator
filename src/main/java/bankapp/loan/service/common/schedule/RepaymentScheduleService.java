package bankapp.loan.service.common.schedule;

import bankapp.loan.model.common.contract.LoanContract;
import bankapp.loan.model.common.schedule.RepaymentSchedule;
import bankapp.loan.model.common.schedule.RepaymentStatus;

import java.time.LocalDate;
import java.util.List;

public interface RepaymentScheduleService {

    void saveRepaymentSchedule(LoanContract loanContract);
    List<RepaymentSchedule> findByRepaymentDateAndStatus(LocalDate localDate , RepaymentStatus repaymentStatus);
}
