package bankapp.loan.servicing.service.core;

import bankapp.loan.common.component.InterestRateCalculator;
import bankapp.loan.exceptions.ActiveLoanContractNotFoundException;
import bankapp.loan.exceptions.InvalidRepaymentStatusException;
import bankapp.loan.product.enums.InterestRateTypeEnum;
import bankapp.loan.product.enums.RepaymentMethodEnum;
import bankapp.loan.servicing.component.AmortizationCalculator;
import bankapp.loan.servicing.component.RepaymentDetail;
import bankapp.loan.servicing.dto.RepaymentAllocationInfo;
import bankapp.loan.servicing.model.LoanAccount;
import bankapp.loan.servicing.model.LoanStatus;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;
import bankapp.loan.servicing.repository.RepaymentScheduleRepository;
import bankapp.loan.underwriting.model.ContractStatus;
import bankapp.loan.underwriting.model.LoanContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;


@Service
public class DefaultRepaymentScheduleService implements RepaymentScheduleService {

    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final InterestRateCalculator interestRateCalculator;
    private final AmortizationCalculator  amortizationCalculator;

    @Autowired
    public DefaultRepaymentScheduleService(RepaymentScheduleRepository repaymentScheduleRepository,
                                           InterestRateCalculator interestRateCalculator,
                                           AmortizationCalculator  amortizationCalculator) {
        this.repaymentScheduleRepository = repaymentScheduleRepository;
        this.interestRateCalculator = interestRateCalculator;
        this.amortizationCalculator = amortizationCalculator;
    }





    @Override
    @Transactional(readOnly = true)
    public List<RepaymentSchedule> getRepaymentSchedules(Long loanAccountId, RepaymentStatus status) {
        if (status == null) throw new InvalidRepaymentStatusException("잘못된 상태 정보");
        return repaymentScheduleRepository.findByLoanAccount_AccountIdAndStatusOrderByDueDateAsc(loanAccountId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentSchedule> getRepaymentSchedules(RepaymentStatus status){
        if (status == null) throw new InvalidRepaymentStatusException("잘못된 상태 정보");
        return repaymentScheduleRepository.findByStatus(status);
    }


    /**
     *  스케줄 상태 변경 (Entity 기반)
     * - 상환 처리 로직 등에서 이미 조회된 객체를 다룰 때 사용
     */
    @Override
    @Transactional
    public void updateRepaymentStatus(RepaymentSchedule schedule, RepaymentStatus targetStatus) {
        // 1. 방어 로직: 이미 해당 상태라면 변경하지 않음 (DB Update 쿼리 방지)
        if (schedule.getStatus() == targetStatus) {
            return;
        }

        // 3. 상태 변경 (JPA Dirty Checking 으로 자동 저장됨)
        schedule.setStatus(targetStatus);
    }

    /**
     * 상환금 충당(Allocation) 로직
     * - Waterfall 방식: 위약금 -> 연체이자 -> 정상이자 -> 원금 순서로 차감
     * - 차감된 결과를 RepaymentAllocationInfo에 담아 반환
     * * @param schedule 차감할 스케줄 엔티티 (Update 대상)
     *  잔액 0 원이면 완료 처리 함
     * @param paymentAmount 사용 가능한 상환 금액 (잔액)
     * @return 이 스케줄에서 실제로 처리된 상세 내역 (DTO)
     */
    @Override
    @Transactional
    public RepaymentAllocationInfo applyPaymentToSchedule(RepaymentSchedule schedule, BigDecimal paymentAmount) {

        // 0. 이미 완료된 스케줄이면 0원 처리 결과 반환
        if (schedule.getStatus() == RepaymentStatus.COMPLETE) {
            return buildEmptyAllocationInfo();
        }

        BigDecimal remainingPayment = paymentAmount; // 차감하면서 줄어들 잔액

        // --- 1. 위약금 (Acceleration Penalty) 충당 ---
        BigDecimal paidPenalty = deductComponent(remainingPayment, schedule.getAccelerationPenaltyAmount());
        if (paidPenalty.compareTo(BigDecimal.ZERO) > 0) {
            schedule.setAccelerationPenaltyAmount(schedule.getAccelerationPenaltyAmount().subtract(paidPenalty));
            remainingPayment = remainingPayment.subtract(paidPenalty);
        }

        // --- 2. 연체 이자 (Delinquent Amount) 충당 ---
        BigDecimal paidDelinquent = deductComponent(remainingPayment, schedule.getDelinquentAmount());
        if (paidDelinquent.compareTo(BigDecimal.ZERO) > 0) {
            schedule.setDelinquentAmount(schedule.getDelinquentAmount().subtract(paidDelinquent));
            remainingPayment = remainingPayment.subtract(paidDelinquent);
        }

        // --- 3. 정상 이자 (Interest Amount) 충당 ---
        BigDecimal paidInterest = deductComponent(remainingPayment, schedule.getInterestAmount());
        if (paidInterest.compareTo(BigDecimal.ZERO) > 0) {
            schedule.setInterestAmount(schedule.getInterestAmount().subtract(paidInterest));
            remainingPayment = remainingPayment.subtract(paidInterest);
        }

        // --- 4. 원금 (Principal Amount) 충당 ---
        BigDecimal paidPrincipal = deductComponent(remainingPayment, schedule.getPrincipalAmount());
        if (paidPrincipal.compareTo(BigDecimal.ZERO) > 0) {
            schedule.setPrincipalAmount(schedule.getPrincipalAmount().subtract(paidPrincipal));
            remainingPayment = remainingPayment.subtract(paidPrincipal);
        }

        // --- 5. 스케줄 상태 업데이트 ---
        // 전체 금액 재계산 (남은 금액 합산)
        BigDecimal remainingTotal = getZeroIfNull(schedule.getPrincipalAmount())
                .add(getZeroIfNull(schedule.getInterestAmount()))
                .add(getZeroIfNull(schedule.getDelinquentAmount()))
                .add(getZeroIfNull(schedule.getAccelerationPenaltyAmount()));

        schedule.setTotalAmount(remainingTotal);

        // 잔액이 0이면 완료 처리
        if (remainingTotal.compareTo(BigDecimal.ZERO) == 0) {
            schedule.setStatus(RepaymentStatus.COMPLETE);
        }

        // --- 6. 결과 DTO 생성 (이번에 갚은 금액만 기록) ---
        // 총 상환액 = 요청금액 - 남은금액
        BigDecimal totalPaidForThisSchedule = paymentAmount.subtract(remainingPayment);

        return RepaymentAllocationInfo.builder()
                .transactionDate(LocalDateTime.now()) // 처리 시점
                .totalRepaymentAmount(totalPaidForThisSchedule)
                .principalAmount(paidPrincipal)
                .interestAmount(paidInterest)
                .delinquentAmount(paidDelinquent)
                .accelerationPenaltyAmount(paidPenalty)
                // loanBalanceAfterTransaction은 이 메서드의 책임이 아니므로 null 또는 상위에서 처리
                .build();
    }

    /** 하루 단순 연체 이자 계산 및 업데이트
     */
    @Override
    @Transactional
    public void updateDailyDelinquent(RepaymentSchedule schedule){

        LoanAccount loanAccount = schedule.getLoanAccount();
        LoanContract loanContract = getActiveContract(loanAccount);

        // 1. 적용할 연체 금리 계산 (약정 금리 + 가산 금리, MAX 15% 제한 로직 포함됨)
        // appliedRate가 null일 경우 0으로 처리하는 방어 로직 추가
        BigDecimal currentAppliedRate = schedule.getAppliedInterestRate() != null
                ? schedule.getAppliedInterestRate()
                : BigDecimal.ZERO;

        BigDecimal PenaltyRatePercent = interestRateCalculator.calculatePenaltyRate(currentAppliedRate);

        // 2. 상환 방식에 따른 계산 기준금액 선정
        BigDecimal targetBaseAmount;
        RepaymentMethodEnum method = loanContract.getRepaymentMethod().getMethodEnum();

        targetBaseAmount = switch (method) { // 원리금 균등
            case EQUAL_PRINCIPAL_INTEREST, EQUAL_PRINCIPAL ->          // 원금 균등
                // 공식: principalAmount 기준
                    schedule.getPrincipalAmount();
            case BULLET ->                   // 만기 일시
                // 공식: 안 낸 이자(interestAmount) 기준
                    schedule.getInterestAmount();
            default -> throw new InvalidRepaymentStatusException("지원하지 않는 상환 방식입니다: " + method);
        };

        // 3. 하루치 연체료 계산
        // 공식: 기준금액 * (연체금리%) * (1/365)
        // 주의: delinquentRatePercent는 '15.0' 같은 퍼센트 값이므로 0.01을 곱해 비율로 변환해야 함
        BigDecimal dailyPenalty = targetBaseAmount
                .multiply(PenaltyRatePercent)           // * 15.0
                .multiply(BigDecimal.valueOf(0.01))        // * 0.01 (백분율 -> 소수점)
                .divide(BigDecimal.valueOf(365), 0, RoundingMode.FLOOR); // / 365 (원 단위 절삭)

        // 4. 엔티티 업데이트 (누적)
        BigDecimal currentDelinquent = schedule.getDelinquentAmount() != null
                ? schedule.getDelinquentAmount()
                : BigDecimal.ZERO;

        BigDecimal newDelinquentAmount = currentDelinquent.add(dailyPenalty);
        schedule.setDelinquentAmount(newDelinquentAmount);

        // 5. Total Amount 재계산 (원금 + 이자 + 누적된 연체료)
        BigDecimal totalAmount = schedule.getInterestAmount()
                .add(schedule.getPrincipalAmount())
                .add(schedule.getDelinquentAmount())
                .add(schedule.getAccelerationPenaltyAmount());


        schedule.setTotalAmount(totalAmount);

    }

    /** 하루 EOD 연체 이자 계산 및 업데이트
     * todo : 대출 원금 계약금 5000 만원 이하는 부과 금지 (예외처리할지 , 아니면 상위에서 로직으로 ?)
     */
    @Override
    @Transactional
    public void updateDailyAcceleration(RepaymentSchedule schedule){

        LoanAccount loanAccount = schedule.getLoanAccount();

        // 1. 적용할 연체 금리 계산 (약정 금리 + 가산 금리, MAX 15% 제한 로직 포함됨)
        // appliedRate가 null일 경우 0으로 처리하는 방어 로직 추가
        BigDecimal currentAppliedRate = schedule.getAppliedInterestRate() != null
                ? schedule.getAppliedInterestRate()
                : BigDecimal.ZERO;

        BigDecimal PenaltyRatePercent = interestRateCalculator.calculatePenaltyRate(currentAppliedRate);

        // 원금에 대해 패널티
        BigDecimal targetBaseAmount = loanAccount.getBalance();


        // 3. 하루치 연체료 계산
        // 공식: 기준금액 * (연체금리%) * (1/365)
        // 주의: delinquentRatePercent는 '15.0' 같은 퍼센트 값이므로 0.01을 곱해 비율로 변환해야 함
        BigDecimal dailyPenalty = targetBaseAmount
                .multiply(PenaltyRatePercent)           // * 15.0
                .multiply(BigDecimal.valueOf(0.01))        // * 0.01 (백분율 -> 소수점)
                .divide(BigDecimal.valueOf(365), 0, RoundingMode.FLOOR); // / 365 (원 단위 절삭)

        // 4. 엔티티 업데이트 (누적)
        BigDecimal currentAccelerationPenaltyAmount = schedule.getAccelerationPenaltyAmount() != null
                ? schedule.getAccelerationPenaltyAmount()
                : BigDecimal.ZERO;

        BigDecimal newAccelerationPenaltyAmount = currentAccelerationPenaltyAmount.add(dailyPenalty);
        schedule.setAccelerationPenaltyAmount(newAccelerationPenaltyAmount);

        // 5. Total Amount 재계산 (원금 + 이자 + 누적된 연체료)
        BigDecimal totalAmount = schedule.getInterestAmount()
                .add(schedule.getPrincipalAmount())
                .add(schedule.getDelinquentAmount())
                .add(schedule.getAccelerationPenaltyAmount());

        schedule.setTotalAmount(totalAmount);
    }

    /** 스케줄 병합
     */
    @Override
    @Transactional
    public void mergeBalance(RepaymentSchedule mergeSchedule , List<RepaymentSchedule> mergedSchedules){
        for(RepaymentSchedule mergedSchedule : mergedSchedules){
            mergeSchedule.setTotalAmount(mergeSchedule.getTotalAmount().add(mergedSchedule.getTotalAmount()));
            mergeSchedule.setInterestAmount(mergeSchedule.getInterestAmount().add(mergedSchedule.getInterestAmount()));
            mergeSchedule.setPrincipalAmount(mergeSchedule.getPrincipalAmount().add(mergedSchedule.getPrincipalAmount()));
            mergeSchedule.setDelinquentAmount(mergeSchedule.getDelinquentAmount().add(mergedSchedule.getDelinquentAmount()));
            mergeSchedule.setAccelerationPenaltyAmount(mergeSchedule.getAccelerationPenaltyAmount().add(mergedSchedule.getAccelerationPenaltyAmount()));

            mergedSchedule.setTotalAmount(BigDecimal.ZERO);
            mergedSchedule.setInterestAmount(BigDecimal.ZERO);
            mergedSchedule.setPrincipalAmount(BigDecimal.ZERO);
            mergedSchedule.setDelinquentAmount(BigDecimal.ZERO);
            mergedSchedule.setAccelerationPenaltyAmount(BigDecimal.ZERO);
        }
    }


    /**
     * 특정 상태(status)이면서, 기한(dueDate)이 기준 날짜(targetDate)보다 같거나 과거인 스케줄 조회
     * * 용도:
     * 1. activateDueSchedules: (PLANNED, 오늘) -> 기한 도래한 것 찾기
     * 2. transitionToDelinquent 등: (PENDING, 어제) -> 납부 기한 지겨서 연체된 것 찾기
     */
    @Override
    @Transactional(readOnly = true)
    public List<RepaymentSchedule> findSchedulesByStatusAndDueDate(RepaymentStatus status, LocalDate targetDate) {
        return repaymentScheduleRepository.findByStatusAndDueDateLessThanEqual(status, targetDate);
    }

    /**
     * 대출 계좌의 상태(LoanStatus)와 스케줄의 상태(RepaymentStatus)가 모두 일치하는 스케줄 조회
     * * 용도:
     * 1. updateBalanceForDelinquent: (DELINQUENT, OVERDUE) -> 이자 갱신 대상
     * 2. updateBalanceForAccelerationNotice: (ACC_NOTICE, CRITICAL_OVERDUE) -> 이자 갱신 대상
     * 3. updateBalanceForAcceleration: (ACCELERATION, ACCELERATED) -> 위약금 갱신 대상
     */
    @Override
    @Transactional(readOnly = true)
    public List<RepaymentSchedule> findSchedulesByLoanAndRepaymentStatus(LoanStatus loanStatus, RepaymentStatus repaymentStatus) {
        return repaymentScheduleRepository.findByLoanAccount_LoanStatusAndStatus(loanStatus, repaymentStatus);
    }

    /**
     * 대출 계좌 상태, 스케줄 상태, 기한 조건을 모두 만족하는 스케줄 조회
     * * 용도:
     * 1. transitionToDelinquent: (NORMAL, PENDING, targetDate) -> 정상 대출 중 연체 발생 건 조회
     * 2. transitionToAccelerationNotice: (DELINQUENT, OVERDUE, targetDate) -> 연체 지속으로 인한 독촉 대상 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<RepaymentSchedule> findSchedulesByLoanStatusAndRepaymentStatusAndDueDate(LoanStatus loanStatus, RepaymentStatus repaymentStatus, LocalDate targetDate) {
        return repaymentScheduleRepository.findByLoanAccount_LoanStatusAndStatusAndDueDateLessThanEqual(loanStatus, repaymentStatus, targetDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentSchedule> findSchedulesByLoanStatusesAndRepaymentStatusAndDueDate(
            List<LoanStatus> loanStatuses,
            RepaymentStatus repaymentStatus,
            LocalDate targetDate) {

        return repaymentScheduleRepository.findByLoanAccount_LoanStatusInAndStatusAndDueDateLessThanEqual(
                loanStatuses, repaymentStatus, targetDate
        );
    }

    @Override
    @Transactional
    public void updateAmount(RepaymentSchedule schedule) {
        LoanContract contract = schedule.getLoanContract();
        LoanAccount account = schedule.getLoanAccount();

        if (contract.getInterestRateType().getTypeEnum() == InterestRateTypeEnum.VARIABLE) {
            // Calculator를 통해 현재 기준(금리, 잔액)으로 다시 계산된 상세 정보 획득
            RepaymentDetail newDetail = amortizationCalculator.getNextRepaymentDetail(contract, account);
            // 계산된 금액으로 스케줄 업데이트
            schedule.setInterestAmount(newDetail.getInterest());
            schedule.setPrincipalAmount(newDetail.getPrincipal());
            schedule.setTotalAmount(newDetail.getPrincipal().add(newDetail.getInterest()));

        }
    }

    /**
     * 특정 컴포넌트(원금, 이자 등)에서 갚을 수 있는 금액 계산
     * Min(내 주머니 사정, 갚아야 할 돈)
     */
    private BigDecimal deductComponent(BigDecimal myMoney, BigDecimal targetComponentAmount) {
        if (targetComponentAmount == null || targetComponentAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return myMoney.min(targetComponentAmount);
    }

    private BigDecimal getZeroIfNull(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private RepaymentAllocationInfo buildEmptyAllocationInfo() {
        return RepaymentAllocationInfo.builder()
                .transactionDate(LocalDateTime.now())
                .totalRepaymentAmount(BigDecimal.ZERO)
                .principalAmount(BigDecimal.ZERO)
                .interestAmount(BigDecimal.ZERO)
                .delinquentAmount(BigDecimal.ZERO)
                .accelerationPenaltyAmount(BigDecimal.ZERO)
                .build();
    }

    /**
     * 내부 헬퍼 메서드: 활성 상태의 대출 계약 조회
     * IllegalStateException 대신 커스텀 예외 사용
     */
    private LoanContract getActiveContract(LoanAccount loanAccount) {
        return loanAccount.getLoanContracts().stream()
                .filter(c -> c.getContractStatus() == ContractStatus.ACTIVE)
                // 최신 버전 우선 (혹시 모를 데이터 꼬임 방지)
                .max(Comparator.comparingInt(LoanContract::getContractVersion))
                .orElseThrow(() -> new ActiveLoanContractNotFoundException(
                        "활성 상태의 대출 계약(LoanContract)이 존재하지 않습니다. Account ID: " + loanAccount.getAccountId()));
    }
}
