package bankapp.loan.underwriting.web.response;

import bankapp.loan.origination.model.ExistingLoan;
import bankapp.loan.underwriting.model.ApplicationStatus;
import bankapp.loan.underwriting.model.LoanApplication;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Data
public class AppliedLoanApplicationResponse {

    private Long loanApplicationId;

    // 회원 정보
    private Long memberId;
    private String memberName;

    // 유저 재무 정보
    private BigDecimal totalAssets;
    private BigDecimal annualIncome;
    private BigDecimal fixedExpenses;
    private BigDecimal totalRemainingBalance;

    // 상품 정보
    private String loanProductName;
    private String loanProductType;

    // 대출 조건
    private String repaymentMethodName;
    private String interestRateTypeName;
    private BigDecimal loanAmount;
    private Integer loanTerm;

    // 금리 상세 (Breakdown)
    private BigDecimal baseRate;
    private BigDecimal productSpread;
    private BigDecimal creditSpread;
    private BigDecimal selectionSpread;
    private BigDecimal finalInterestRate;

    // 심사 지표
    private BigDecimal debtServiceRatio;

    // 관리 정보
    private ApplicationStatus applicationStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AppliedLoanApplicationResponse from(LoanApplication app) {
        AppliedLoanApplicationResponse response = new AppliedLoanApplicationResponse();

        response.setLoanApplicationId(app.getLoanApplicationId());

        response.setMemberId(app.getMember().getMemberId());
        response.setMemberName(app.getMember().getName());

        response.setTotalAssets(app.getTotalAssets());
        response.setAnnualIncome(app.getAnnualIncome());
        response.setFixedExpenses(app.getFixedExpenses());
        BigDecimal totalRemainingBalance = BigDecimal.ZERO;
        for(ExistingLoan existingLoan : app.getExistingLoans()) {
            totalRemainingBalance = totalRemainingBalance.add(existingLoan.getRemainingBalance());
        }
        response.setTotalRemainingBalance(totalRemainingBalance);

        response.setLoanProductName(app.getLoanProduct().getLoanProductName());
        response.setLoanProductType(app.getLoanProduct().getLoanType());

        response.setRepaymentMethodName(app.getRepaymentMethod().getMethodName());
        response.setInterestRateTypeName(app.getInterestRateType().getTypeName());
        response.setLoanAmount(app.getLoanAmount());
        response.setLoanTerm(app.getLoanTerm());

        response.setBaseRate(app.getBaseRate());
        response.setProductSpread(app.getProductSpread());
        response.setCreditSpread(app.getCreditSpread());
        response.setSelectionSpread(app.getSelectionSpread());
        response.setFinalInterestRate(app.getFinalInterestRate());
        response.setDebtServiceRatio(app.getDebtServiceRatio());

        response.setApplicationStatus(app.getApplicationStatus());

        response.setCreatedAt(app.getCreatedAt());
        response.setUpdatedAt(app.getUpdatedAt());

        return response;
    }
}
