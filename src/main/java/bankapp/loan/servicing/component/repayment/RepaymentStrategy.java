package bankapp.loan.servicing.component.repayment;

import bankapp.loan.servicing.model.LoanStatus;
import bankapp.loan.servicing.model.RepaymentSchedule;

import java.math.BigDecimal;

public interface RepaymentStrategy {

    // 이 전략이 어떤 LoanStatus를 담당하는지 반환
    LoanStatus getSupportedStatus();

    // 실제 상환 로직 수행
    void execute(RepaymentSchedule schedule , BigDecimal repaymentAmount);
}
