package bankapp.loan.servicing.service;

import bankapp.account.model.account.LoanAccount;
import bankapp.loan.servicing.component.AmortizationCalculator;
import bankapp.loan.servicing.component.RepaymentDetail;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;
import bankapp.loan.servicing.repository.RepaymentScheduleRepository;
import bankapp.loan.underwriting.model.LoanContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class DefaultLoanRepaymentService implements LoanRepaymentService{

    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final AmortizationCalculator amortizationCalculator;

    @Autowired
    public DefaultLoanRepaymentService(RepaymentScheduleRepository repaymentScheduleRepository
                                        ,AmortizationCalculator amortizationCalculator) {
        this.repaymentScheduleRepository = repaymentScheduleRepository;
        this.amortizationCalculator = amortizationCalculator;
    }


    @Override
    @Transactional
    public void saveRepaymentSchedule(LoanAccount loanAccount , LoanContract loanContract){
        List<RepaymentDetail> calculationDetails = amortizationCalculator.calculate(loanContract ,loanAccount);

        for(RepaymentDetail detail : calculationDetails){
            RepaymentSchedule schedule = new RepaymentSchedule();

            schedule.setLoanAccount(loanAccount);
            schedule.setLoanContract(loanContract);
            schedule.setTotalAmount(detail.getInterest().add(detail.getPrincipal()));
            schedule.setInterestAmount(detail.getInterest());
            schedule.setPrincipalAmount(detail.getPrincipal());
            schedule.setDelinquentAmount(BigDecimal.ZERO);
            schedule.setAccelerationPenaltyAmount(BigDecimal.ZERO);
            schedule.setDueDate(detail.getDueDate());
            schedule.setStatus(RepaymentStatus.PLANNED);

            repaymentScheduleRepository.save(schedule);
        }

    }



}
