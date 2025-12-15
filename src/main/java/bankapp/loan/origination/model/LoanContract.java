package bankapp.loan.origination.model;

import bankapp.account.model.account.LoanAccount;
import bankapp.loan.product.model.LoanProduct;
import bankapp.loan.product.model.InterestRateType;
import bankapp.loan.product.model.RepaymentMethod;
import bankapp.member.model.Member;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;


@Entity
@Getter
@Setter
@Builder
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@AllArgsConstructor
public class LoanContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanContractId;

    // TODO : 계약:계좌 = M:1 관계 , 금리인하요구 등의 상황 발생 가능
    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "account_id" , nullable = false)
    private LoanAccount loanAccount;

    // TODO : 2번째 계약부터는 신청서가 없을수도 있음
    @OneToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "loan_application_id")
    private LoanApplication loanApplication;

    // TODO : member , product 에서도 1:다 관계 연결해줘야
    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "member_id", nullable = false , updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "loan_product_id", nullable = false , updatable = false)
    private LoanProduct loanProduct;

    // todo : 금리 인하 요구권 , 상품 금리 변경으로 변할수도 있다고 가정
    @Column(nullable = false)
    private BigDecimal contractProductSpread;

    @Column(nullable = false)
    private BigDecimal contractCreditSpread;

    // todo : 고정금리 상품시 적용되는 고정 금리를 뜻함
    @Column(nullable = false)
    private BigDecimal contractBaseRate;

    // todo : 거치식은 없다고 가정
    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "repayment_method_id", nullable = false)
    private RepaymentMethod repaymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_rate_type_id", nullable = false)
    private InterestRateType interestRateType;

    // todo : 기간 연장시 , 새로운 계약서를 쓴다고 가정
    @Column(nullable = false , updatable = false)
    private BigDecimal loanAmount;

    @Column(nullable = false , updatable = false)
    private Integer loanTerm;

    @Column(nullable = false , updatable = false)
    private LocalDateTime contractDate;

    @Column(nullable = false , updatable = false)
    private LocalDateTime maturityDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status;

    // todo : 계약의 버전관리
    @Column(nullable = false)
    private Integer contractVersion;


    // 연관관계 편의 메서드
    public void setLoanAccount(LoanAccount loanAccount) {
        if(this.loanAccount != null){
            this.loanAccount.getLoanContracts().remove(this);
        }
        this.loanAccount = loanAccount;
        this.loanAccount.getLoanContracts().add(this);
    }


}
