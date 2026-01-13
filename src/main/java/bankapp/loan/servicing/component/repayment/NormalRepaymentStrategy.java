package bankapp.loan.servicing.component.repayment;

import bankapp.loan.servicing.model.LoanStatus;
import bankapp.loan.exceptions.InvalidRepaymentStatusException;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class NormalRepaymentStrategy implements RepaymentStrategy{


    @Override
    public LoanStatus getSupportedStatus(){
        return LoanStatus.NORMAL;
    }

    @Override
    @Transactional
    public void execute(RepaymentSchedule schedule , BigDecimal repaymentAmount){

        if(schedule.getStatus() != RepaymentStatus.PENDING) throw new InvalidRepaymentStatusException("유효한 상환 스케줄 상태가 아닙니다.");

        BigDecimal scheduledAmount = schedule.getTotalAmount();












    }
}
