package bankapp.loan.servicing.component.repayment;

import bankapp.loan.servicing.model.LoanStatus;
import bankapp.loan.servicing.model.RepaymentSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DelinquentRepaymentStrategy implements RepaymentStrategy{

    @Override
    public LoanStatus getSupportedStatus() {
        return LoanStatus.DELINQUENT;
    }

    @Override
    public void execute(RepaymentSchedule schedule) {

    }

}
