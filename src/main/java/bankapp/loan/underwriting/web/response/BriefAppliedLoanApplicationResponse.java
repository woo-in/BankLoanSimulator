package bankapp.loan.underwriting.web.response;

import bankapp.loan.underwriting.model.ApplicationStatus;
import bankapp.loan.underwriting.model.LoanApplication;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Data
public class BriefAppliedLoanApplicationResponse {

    private Long loanApplicationId;

    // 회원 정보 (ID와 이름)
    private Long memberId;
    private String memberName;

    // 상품 정보
    private String loanProductName;
    private String loanProductType;

    // 대출 핵심 지표
    private BigDecimal finalInterestRate;
    private BigDecimal debtServiceRatio;

    // 상태 및 시간
    private ApplicationStatus applicationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BriefAppliedLoanApplicationResponse from(LoanApplication app) {
        BriefAppliedLoanApplicationResponse response = new BriefAppliedLoanApplicationResponse();

        response.setLoanApplicationId(app.getLoanApplicationId());

        // Member 연관관계 탐색
        response.setMemberId(app.getMember().getMemberId());
        response.setMemberName(app.getMember().getName());

        // LoanProduct 연관관계 탐색
        response.setLoanProductName(app.getLoanProduct().getLoanProductName());
        response.setLoanProductType(app.getLoanProduct().getLoanType());

        // 대출 정보 매핑
        response.setFinalInterestRate(app.getFinalInterestRate());
        response.setDebtServiceRatio(app.getDebtServiceRatio());
        response.setApplicationStatus(app.getApplicationStatus());

        response.setCreatedAt(app.getCreatedAt());
        response.setUpdatedAt(app.getUpdatedAt());

        return response;
    }
}