package bankapp.loan.servicing.service;

import bankapp.loan.underwriting.model.LoanContract;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;

import java.time.LocalDate;
import java.util.List;

public interface RepaymentScheduleService {

    void saveRepaymentSchedule(LoanContract loanContract);
    List<RepaymentSchedule> findByRepaymentDateAndStatus(LocalDate localDate , RepaymentStatus repaymentStatus);
}
