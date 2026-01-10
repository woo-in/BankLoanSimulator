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
import java.time.LocalDate;
import java.util.List;

import static bankapp.loan.product.enums.InterestRateTypeEnum.VARIABLE;

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
        List<RepaymentDetail> calculationDetails = amortizationCalculator.getPlannedRepaymentDetails(loanContract ,loanAccount);

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

    @Override
    @Transactional
    public void prepareUpcomingRepaymentSchedule(LocalDate date , int daysToAdd){
        LocalDate billingHorizon = date.plusDays(daysToAdd);

        List<RepaymentSchedule> schedules = repaymentScheduleRepository.findByStatusAndDueDateLessThanEqual(
                RepaymentStatus.PLANNED,
                billingHorizon
        );
        for (RepaymentSchedule schedule : schedules) {
            LoanContract contract = schedule.getLoanContract();
            LoanAccount account = schedule.getLoanAccount();

            // 1. 변동 금리 상품인 경우 재산정 로직 수행
            if (contract.getInterestRateType().getTypeEnum() == VARIABLE) {
                // Calculator를 통해 현재 기준(금리, 잔액)으로 다시 계산된 상세 정보 획득
                RepaymentDetail newDetail = amortizationCalculator.getNextRepaymentDetail(contract, account);

                // 계산된 금액으로 스케줄 업데이트 (주의: 날짜는 기존 PLANNED 날짜 유지)
                schedule.setInterestAmount(newDetail.getInterest());
                schedule.setPrincipalAmount(newDetail.getPrincipal());

                // 총 청구액 업데이트 (원금 + 이자)
                BigDecimal totalAmount = newDetail.getPrincipal().add(newDetail.getInterest());
                schedule.setTotalAmount(totalAmount);
            }

            // 2. 상태 변경 (청구 확정)
            schedule.setStatus(RepaymentStatus.PENDING);

            // 3. (중요) 해당 회차 청구가 확정되었으므로, Account의 회차 정보도 동기화/검증 로직이 필요할 수 있음
            // 예: account.advanceInstallment(); (설계에 따라 여기서 할지, 실제 납부 후 할지 결정)
        }

        // Dirty Checking에 의해 변경사항(금액, 상태) 자동 저장
    }

}
