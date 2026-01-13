package bankapp.loan.servicing.component;

import bankapp.loan.servicing.model.LoanAccount;
import bankapp.loan.common.component.InterestRateCalculator;
import bankapp.loan.underwriting.model.LoanContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class AmortizationCalculator {

    // 금융 계산을 위한 정밀도 설정 (소수점 10자리까지, 반올림)
    private static final int CALCULATION_SCALE = 10;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final MathContext MC = new MathContext(CALCULATION_SCALE, ROUNDING_MODE);
    private final InterestRateCalculator interestRateCalculator;

    @Autowired
    public AmortizationCalculator(InterestRateCalculator interestRateCalculator) {
        this.interestRateCalculator = interestRateCalculator;
    }


    /**
     * 최초 계약시 , 전체 예상 상환 상세 정보를 반환
     * @param loanContract 대출 계약서
     * @param loanAccount 대출 계좌
     * @return List<RepaymentDetail> 날짜 별 상환 상세 정보
     */
    public List<RepaymentDetail> getPlannedRepaymentDetails(LoanContract loanContract , LoanAccount loanAccount) {


        BigDecimal appliedRate = calculateAppliedRate(loanContract);


        BigDecimal principal = loanContract.getLoanAmount();
        int term = loanContract.getLoanTerm();

        int paymentDay = loanAccount.getPaymentDay();
        LocalDate startDate = LocalDate.now();

        // 3. 상환 방식에 따라 다른 계산 전략 선택
        return switch (loanContract.getRepaymentMethod().getMethodEnum()) {
            case EQUAL_PRINCIPAL_INTEREST -> calculateEqualInstallment(principal, appliedRate, term , startDate , paymentDay);
            case EQUAL_PRINCIPAL -> calculateEqualPrincipal(principal, appliedRate, term , startDate ,paymentDay);
            case BULLET -> calculateBulletPayment(principal, appliedRate, term , startDate , paymentDay);
        };
    }

    /**
     * 다음 회차에 납부할 상환 상세 정보를 반환 (주로 변동금리 시 , 호출)
     * @param loanContract 대출 계약서
     * @param loanAccount 대출 계좌
     * @return RepaymentDetail 해당 회차의 상환 상세 정보
     */
    public RepaymentDetail getNextRepaymentDetail(LoanContract loanContract , LoanAccount loanAccount) {
        return switch (loanContract.getRepaymentMethod().getMethodEnum()) {
            case EQUAL_PRINCIPAL_INTEREST -> calculateSingleEqualInstallment(loanContract , loanAccount);
            case EQUAL_PRINCIPAL -> calculateSingleEqualPrincipal(loanContract , loanAccount);
            case BULLET -> calculateSingleBulletPayment(loanContract , loanAccount);
        };
    }




    /**
     * 핵심설명서용 첫 달 예상 납부액 계산
     * * @param loanAmount 대출 신청 금액
     * @param loanTerm 대출 기간 (개월)
     * @param annualInterestRate 연 이자율 (%)
     * @param repaymentMethodName 상환 방법 이름 (코드 또는 한글)
     * @return 첫 달 납부 예상 금액 (원금 + 이자, 원 단위 반올림)
     */
    public BigDecimal calculateFirstMonthEstimatedPayment(BigDecimal loanAmount,
                                                          Integer loanTerm,
                                                          BigDecimal annualInterestRate,
                                                          String repaymentMethodName) {

        // 유효성 검사
        if (loanAmount == null || loanAmount.compareTo(BigDecimal.ZERO) == 0 ||
                loanTerm == null || loanTerm == 0 ||
                annualInterestRate == null) {
            return BigDecimal.ZERO;
        }

        // 월 이율 변환 (연이율 / 12 / 100)
        BigDecimal monthlyRate = annualInterestRate.divide(BigDecimal.valueOf(100), MC)
                .divide(BigDecimal.valueOf(12), MC);

        // 1. 원금만기일시상환 (Bullet)
        // 첫 달은 이자만 납부 (원금 상환 0원)
        if (repaymentMethodName.contains("만기") || repaymentMethodName.contains("BULLET")) {
            return loanAmount.multiply(monthlyRate).setScale(0, ROUNDING_MODE);
        }

        // 2. 원리금균등분할상환 (Equal Principal & Interest - PMT)
        // PMT = P * r * (1+r)^n / ((1+r)^n - 1)
        if (repaymentMethodName.contains("원리금") || repaymentMethodName.contains("EQUAL_PRINCIPAL_INTEREST")) {
            // (1+r)^n
            BigDecimal onePlusRatePow = BigDecimal.ONE.add(monthlyRate).pow(loanTerm, MC);

            BigDecimal numerator = loanAmount.multiply(monthlyRate).multiply(onePlusRatePow);
            BigDecimal denominator = onePlusRatePow.subtract(BigDecimal.ONE);

            if (denominator.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

            return numerator.divide(denominator, 0, ROUNDING_MODE);
        }

        // 3. 원금균등분할상환 (Equal Principal)
        // 첫 달 납부액 = (총원금 / 기간) + (총원금 * 월이율)
        // * 첫 달이 이자가 가장 많고 갈수록 줄어드는 구조
        if (repaymentMethodName.contains("원금") || repaymentMethodName.contains("EQUAL_PRINCIPAL")) {
            BigDecimal monthlyPrincipal = loanAmount.divide(BigDecimal.valueOf(loanTerm), MC);
            BigDecimal monthlyInterest = loanAmount.multiply(monthlyRate);

            return monthlyPrincipal.add(monthlyInterest).setScale(0, ROUNDING_MODE);
        }

        return BigDecimal.ZERO;
    }



    /**
     * 1. 원리금균등상환 (Equal Installment)
     * M = P * (r * (1+r)^n) / ((1+r)^n - 1)
     */
    private List<RepaymentDetail> calculateEqualInstallment(BigDecimal principal, BigDecimal appliedRate, int term, LocalDate startDate, int paymentDay) {
        List<RepaymentDetail> details = new ArrayList<>();
        BigDecimal remainingPrincipal = principal;

        BigDecimal monthlyRate = calculateMonthlyRate(appliedRate);

        // 월 상환금(PMT) 계산
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = onePlusRate.pow(term, MC);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);

        BigDecimal monthlyPayment = numerator.divide(denominator, 0, ROUNDING_MODE);

        for (int i = 1; i <= term; i++) {
            BigDecimal interest = remainingPrincipal.multiply(monthlyRate).setScale(0, ROUNDING_MODE);
            BigDecimal principalPayment = monthlyPayment.subtract(interest);

            // 마지막 회차 보정
            if (i == term) {
                principalPayment = remainingPrincipal;
                interest = monthlyPayment.subtract(principalPayment);
            }

            remainingPrincipal = remainingPrincipal.subtract(principalPayment);

            // 날짜 계산 및 리스트 추가
            LocalDate dueDate = calculateDueDate(startDate, i, paymentDay);
            details.add(new RepaymentDetail(dueDate, principalPayment, interest , appliedRate));
        }
        return details;
    }

    /**
     * 2. 원금균등상환 (Equal Principal)
     */
    private List<RepaymentDetail> calculateEqualPrincipal(BigDecimal principal, BigDecimal appliedRate, int term, LocalDate startDate, int paymentDay) {

        BigDecimal monthlyRate = calculateMonthlyRate(appliedRate);

        List<RepaymentDetail> details = new ArrayList<>();

        BigDecimal principalPayment = principal.divide(BigDecimal.valueOf(term), 0, ROUNDING_MODE);
        BigDecimal remainingPrincipal = principal;

        for (int i = 1; i <= term; i++) {
            BigDecimal interest = remainingPrincipal.multiply(monthlyRate).setScale(0, ROUNDING_MODE);

            if (i == term) {
                principalPayment = remainingPrincipal;
            }

            remainingPrincipal = remainingPrincipal.subtract(principalPayment);

            LocalDate dueDate = calculateDueDate(startDate, i, paymentDay);
            details.add(new RepaymentDetail(dueDate, principalPayment, interest , appliedRate));
        }
        return details;
    }

    /**
     * 3. 원금만기일시상환 (Bullet Payment)
     */
    private List<RepaymentDetail> calculateBulletPayment(BigDecimal principal, BigDecimal appliedRate, int term, LocalDate startDate, int paymentDay) {

        BigDecimal monthlyRate = calculateMonthlyRate(appliedRate);


        List<RepaymentDetail> details = new ArrayList<>();

        BigDecimal interest = principal.multiply(monthlyRate).setScale(0, ROUNDING_MODE);

        for (int i = 1; i <= term; i++) {
            LocalDate dueDate = calculateDueDate(startDate, i, paymentDay);

            if (i < term) {
                details.add(new RepaymentDetail(dueDate, BigDecimal.ZERO, interest,appliedRate));
            } else {
                details.add(new RepaymentDetail(dueDate, principal, interest,appliedRate));
            }
        }
        return details;
    }

    /**
     * 1. 원리금균등상환 - 단건 계산 (재산정)
     */
    private RepaymentDetail calculateSingleEqualInstallment(LoanContract loanContract, LoanAccount loanAccount) {
        // 1. 변동 금리(월이율) 계산
        BigDecimal appliedRate = calculateAppliedRate(loanContract);
        BigDecimal monthlyRate = calculateMonthlyRate(appliedRate);

        // 2. 변수 준비
        BigDecimal remainingBalance = loanAccount.getOutstandingPrincipal();
        int currentSequence = loanAccount.getCurrentInstallmentNumber();
        int remainingTerm = loanContract.getLoanTerm() - currentSequence + 1;

        // 방어 로직
        if (remainingTerm <= 0 || remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return new RepaymentDetail(LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO,appliedRate);
        }

        // 3. PMT 재산정
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = onePlusRate.pow(remainingTerm, MC);
        BigDecimal numerator = remainingBalance.multiply(monthlyRate).multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);

        BigDecimal newMonthlyPayment = numerator.divide(denominator, 0, ROUNDING_MODE);

        // 4. 이자 및 원금 분리
        BigDecimal interest = remainingBalance.multiply(monthlyRate).setScale(0, ROUNDING_MODE);
        BigDecimal principal = newMonthlyPayment.subtract(interest);

        // 5. 마지막 회차 보정
        if (currentSequence == loanContract.getLoanTerm()) {
            principal = loanAccount.getBalance();
        }

        return createRepaymentDetail(loanContract, loanAccount, principal, interest,appliedRate);
    }

    /**
     * 2. 원금균등상환 - 단건 계산
     */
    private RepaymentDetail calculateSingleEqualPrincipal(LoanContract loanContract, LoanAccount loanAccount) {
        // 1. 변동 금리 계산
        BigDecimal appliedRate = calculateAppliedRate(loanContract);
        BigDecimal monthlyRate = calculateMonthlyRate(appliedRate);

        // 2. 이자 계산 (현재 잔액 기준)
        BigDecimal remainingBalance = loanAccount.getOutstandingPrincipal();
        BigDecimal interest = remainingBalance.multiply(monthlyRate).setScale(0, ROUNDING_MODE);

        // 3. 원금 계산 (총 원금 / 총 기간) -> 불변
        BigDecimal principal;
        if (Objects.equals(loanAccount.getCurrentInstallmentNumber(), loanContract.getLoanTerm())) {
            principal = loanAccount.getBalance();
        } else {
            principal = loanContract.getLoanAmount()
                    .divide(BigDecimal.valueOf(loanContract.getLoanTerm()), 0, ROUNDING_MODE);
        }

        return createRepaymentDetail(loanContract, loanAccount, principal, interest,appliedRate);
    }

    /**
     * 3. 만기일시상환 - 단건 계산
     */
    private RepaymentDetail calculateSingleBulletPayment(LoanContract loanContract, LoanAccount loanAccount) {
        // 1. 변동 금리 계산
        BigDecimal appliedRate = calculateAppliedRate(loanContract);
        BigDecimal monthlyRate = calculateMonthlyRate(appliedRate);

        // 2. 이자 및 원금 계산
        BigDecimal interest = loanAccount.getOutstandingPrincipal()
                .multiply(monthlyRate).setScale(0, ROUNDING_MODE);

        BigDecimal principal = BigDecimal.ZERO;
        if (Objects.equals(loanAccount.getCurrentInstallmentNumber(), loanContract.getLoanTerm())) {
            principal = loanAccount.getOutstandingPrincipal();
        }

        return createRepaymentDetail(loanContract, loanAccount, principal, interest,appliedRate);
    }





    /**
     * [Helper] 납부 예정일 계산 로직
     * 시작일로부터 monthOffset 만큼 뒤의 달로 이동하되, 날짜는 paymentDay로 고정합니다.
     * 예: 1월 31일 시작, paymentDay가 28일 -> 2월 28일, 3월 28일...
     * 예: paymentDay가 31일인데 2월인 경우 -> 2월 28일(혹은 29일)로 자동 조정
     */
    private LocalDate calculateDueDate(LocalDate startDate, int monthOffset, int paymentDay) {
        // 1. 해당 회차의 달(Month) 계산
        LocalDate targetMonthDate = startDate.plusMonths(monthOffset);

        // 2. 해당 달의 마지막 날짜 구하기 (예: 2월이면 28 or 29)
        int lastDayOfTargetMonth = targetMonthDate.lengthOfMonth();

        // 3. paymentDay와 그 달의 말일 중 작은 값을 선택 (예: 납입일 31일인데 2월이면 28일로 설정)
        int realPaymentDay = Math.min(paymentDay, lastDayOfTargetMonth);

        return targetMonthDate.withDayOfMonth(realPaymentDay);
    }

    /**
     * 현재 시점의 변동 금리를 반영하여 '월 이율'을 반환
     */
    private BigDecimal calculateAppliedRate(LoanContract loanContract){
        return loanContract.getCreditSpread()
                .add(loanContract.getProductSpread())
                .add(loanContract.getSelectionSpread())
                .add(interestRateCalculator.calculateBaseRate());
    }

    private BigDecimal calculateMonthlyRate(BigDecimal appliedRate){
        // 연이율 -> 소수점 -> 월이율
        return appliedRate
                .divide(BigDecimal.valueOf(100), MC)
                .divide(BigDecimal.valueOf(12), MC);
    }


    /**
     * 날짜 계산을 포함하여 RepaymentDetail 객체 생성
     */
    private RepaymentDetail createRepaymentDetail(LoanContract loanContract, LoanAccount loanAccount,
                                                  BigDecimal principal, BigDecimal interest , BigDecimal appliedRate) {
        LocalDate baseDate = loanContract.getContractDate() != null ?
                loanContract.getContractDate().toLocalDate() : LocalDate.now();

        LocalDate dueDate = calculateDueDate(
                baseDate,
                loanAccount.getCurrentInstallmentNumber(),
                loanAccount.getPaymentDay()
        );

        return new RepaymentDetail(dueDate, principal, interest,appliedRate);
    }

}




