package bankapp.loan.servicing.service;

import bankapp.account.model.account.LoanAccount;
import bankapp.loan.underwriting.model.LoanContract;
import bankapp.loan.servicing.model.RepaymentDetail;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;
import bankapp.loan.servicing.repository.RepaymentScheduleRepository;
import bankapp.loan.servicing.component.AmortizationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class DefaultRepaymentScheduleService implements RepaymentScheduleService {

    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final AmortizationCalculator amortizationCalculator;


    @Autowired
    public DefaultRepaymentScheduleService(RepaymentScheduleRepository repaymentScheduleRepository,
                                           AmortizationCalculator amortizationCalculator) {
        this.repaymentScheduleRepository = repaymentScheduleRepository;
        this.amortizationCalculator = amortizationCalculator;
    }

    // todo : 고객 지정일에 갚는 경우 고려
    @Override
    @Transactional
    public void saveRepaymentSchedule(LoanContract loanContract){

        // 1. 계산에 필요한 기본 정보들을 LoanContract 에서 가져옵니다.
        LoanAccount loanAccount = loanContract.getLoanAccount();
        LocalDate baseDate = loanContract.getContractDate().toLocalDate(); // 대출 실행일(계약일)
        List<RepaymentDetail> calculationDetails = amortizationCalculator.calculate(loanContract);

        // 3. 대출 기간(개월 수)만큼 루프
        for(RepaymentDetail detail : calculationDetails){
            RepaymentSchedule schedule = new RepaymentSchedule();

            schedule.setPrincipalAmount(detail.getPrincipal());
            schedule.setInterestAmount(detail.getInterest());
            schedule.setLoanAccount(loanAccount);
            // 상환일 계산: LocalDate가 말일 처리를 자동으로 수행합니다.
            schedule.setRepaymentDate(baseDate.plusMonths(detail.getSequence()));
            schedule.setRepaymentSequence(detail.getSequence());
            schedule.setStatus(RepaymentStatus.PENDING);

            repaymentScheduleRepository.save(schedule);
        }
    }

    @Override
    @Transactional
    public List<RepaymentSchedule> findByRepaymentDateAndStatus(LocalDate localDate ,RepaymentStatus repaymentStatus){
        return repaymentScheduleRepository.findByRepaymentDateAndStatus(localDate , repaymentStatus);
    }




}
