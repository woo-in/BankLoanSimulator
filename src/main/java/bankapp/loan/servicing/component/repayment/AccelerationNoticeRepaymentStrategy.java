package bankapp.loan.servicing.component.repayment;

import bankapp.loan.servicing.model.LoanStatus;
import bankapp.loan.servicing.model.RepaymentSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccelerationNoticeRepaymentStrategy implements RepaymentStrategy{

    @Override
    public LoanStatus getSupportedStatus() {
        return LoanStatus.ACCELERATION_NOTICE;
    }

    @Override
    public void execute(RepaymentSchedule schedule) {

    }
}
