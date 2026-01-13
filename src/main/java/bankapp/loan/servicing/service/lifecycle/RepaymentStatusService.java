package bankapp.loan.servicing.service.lifecycle;

import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;

public interface RepaymentStatusService {

    void changeRepaymentStatus(RepaymentSchedule schedule, RepaymentStatus targetStatus);

}
