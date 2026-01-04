package bankapp.loan.underwriting.model;

import bankapp.account.model.account.Account;
import bankapp.loan.origination.model.ExistingLoan;
import bankapp.loan.origination.model.PendingLoanApplication;
import bankapp.loan.product.model.LoanProduct;
import bankapp.loan.product.model.InterestRateType;
import bankapp.loan.product.model.RepaymentMethod;
import bankapp.member.model.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanApplicationId;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "member_id" , nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "loan_product_id" , nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "repayment_method_id" , nullable = false)
    private RepaymentMethod repaymentMethod;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "interest_rate_type_id" , nullable = false)
    private InterestRateType interestRateType;

    @Column(nullable = false)
    private BigDecimal loanAmount;
    @Column(nullable = false)
    private Integer loanTerm;

    // 유저 재무 정보
    @Column(nullable = false)
    private BigDecimal totalAssets;
    @Column(nullable = false)
    private BigDecimal annualIncome;
    @Column(nullable = false)
    private BigDecimal fixedExpenses;



    // 기존 대출 목록 (ExistingLoan) - 1:N 관계 설정
    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExistingLoan> existingLoans = new ArrayList<>();

    // 금리 정보
    @Column(nullable = false)
    private BigDecimal baseRate;
    @Column(nullable = false)
    private BigDecimal productSpread;
    @Column(nullable = false)
    private BigDecimal creditSpread;
    @Column(nullable = false)
    private BigDecimal selectionSpread;
    @Column(nullable = false)
    private BigDecimal finalInterestRate;
    @Column(nullable = false)
    private BigDecimal debtServiceRatio;

    // 승인 정보 (approved~)
    private BigDecimal approvedLoanAmount;
    private Integer approvedLoanTerm;
    private BigDecimal approvedBaseRate;
    private BigDecimal approvedProductSpread;
    private BigDecimal approvedCreditSpread;
    private BigDecimal approvedSelectionSpread;

    private BigDecimal approvedFinalInterestRate;
    private BigDecimal approvedDebtServiceRatio;

    // 추가 정보 (계약 바로 전 입력)
    // 상환 계좌 / 대출 수령 계좌 / 결제일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repayment_account_id")
    private Account repaymentAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursement_account_id")
    private Account disbursementAccount;

//  1일 ~ 28일 사이로 제한 (2월 이슈 방지)
    private Integer paymentDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus applicationStatus;

    @Column(columnDefinition = "TEXT")
    private String messageToCustomer;



    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // [수정됨] createFrom 메서드
    public static LoanApplication createFrom(PendingLoanApplication pending) {

        LoanApplication app = new LoanApplication();

        app.setMember(pending.getMember());
        app.setLoanProduct(pending.getLoanProduct());

        app.setLoanAmount(pending.getRequestLoanAmount());
        app.setLoanTerm(pending.getRequestLoanTerm());
        app.setRepaymentMethod(pending.getRepaymentMethod());
        app.setInterestRateType(pending.getInterestRateType());

        app.setTotalAssets(pending.getTotalAssets());
        app.setAnnualIncome(pending.getAnnualIncome());
        app.setFixedExpenses(pending.getFixedExpenses());

        if (pending.getExistingLoans() != null) {
            for (ExistingLoan origin : pending.getExistingLoans()) {
                // 1. 새로운 ExistingLoan 객체 생성 (데이터 복사)
                ExistingLoan newLoan = ExistingLoan.builder()
                        .loanProductName(origin.getLoanProductName())
                        .loanType(origin.getLoanType())
                        .loanAmount(origin.getLoanAmount())
                        .remainingBalance(origin.getRemainingBalance())
                        .loanTerm(origin.getLoanTerm())
                        .repaymentMethodName(origin.getRepaymentMethodName())
                        .interestRateTypeName(origin.getInterestRateTypeName())
                        .totalInterestRate(origin.getTotalInterestRate())
                        .isExternal(origin.isExternal())
                        .build();

                // 2. 연관관계 설정 (편의 메서드 사용)
                newLoan.setLoanApplication(app);
            }
        }

        app.setBaseRate(pending.getBaseRate());
        app.setProductSpread(pending.getProductSpread());
        app.setCreditSpread(pending.getCreditSpread());
        app.setSelectionSpread(pending.getSelectionSpread());
        app.setFinalInterestRate(pending.getFinalInterestRate());
        app.setDebtServiceRatio(pending.getDebtServiceRatio());
        app.setApplicationStatus(ApplicationStatus.APPLIED);

        return app;
    }
}