package bankapp.loan.product.model;

import bankapp.loan.exceptions.InvalidLoanProduct;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
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
@DiscriminatorColumn(name = "loan_type")
@SuperBuilder
@NoArgsConstructor
public abstract class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanProductId;

    @Column(nullable = false)
    private String loanProductName;

    @Column(nullable = false, unique = true)
    private String loanProductSlug;

    @Column(nullable = false)
    private String loanProductDescription;

    @Column(name = "loan_type", insertable = false, updatable = false)
    private String loanType;

    @Column(nullable = false)
    private BigDecimal maxLoanAmount;

    @Column(nullable = false)
    private BigDecimal minLoanAmount;

    @Column(nullable = false)
    private BigDecimal applicationAmountUnit;

    @Column(nullable = false)
    private Integer maxLoanTerm;

    @Column(nullable = false)
    private Integer minLoanTerm;

    @Column(nullable = false)
    private Integer applicationTermUnit;


    // todo : 가산금리 계산로직 수정 필수
    @Column(nullable = false)
    private BigDecimal defaultSpread;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "loanProduct")
    private List<LoanProductInterestRateTypeOption>  interestRateTypeOptions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "loanProduct")
    private List<LoanProductRepaymentOption>  repaymentOptions = new ArrayList<>();



    public List<BigDecimal> getAvailableAmounts() throws InvalidLoanProduct {
        List<BigDecimal> amounts = new ArrayList<>();

        if(this.applicationAmountUnit.compareTo(BigDecimal.ZERO) <= 0 || this.maxLoanAmount.compareTo(this.minLoanAmount) < 0){
            throw new InvalidLoanProduct("대출 상품 설정 오류");
        }

        BigDecimal current = this.minLoanAmount;
        while (current.compareTo(this.maxLoanAmount) <= 0) {
            amounts.add(current);
            current = current.add(this.applicationAmountUnit);
        }
        return amounts;
    }

    public List<Integer> getAvailableTerms() throws InvalidLoanProduct {
        List<Integer> terms = new ArrayList<>();

        if(this.applicationTermUnit <= 0 || this.maxLoanTerm < this.minLoanTerm){
            throw new InvalidLoanProduct("대출 상품 설정 오류");
        }

        for (int term = this.minLoanTerm; term <= this.maxLoanTerm; term += this.applicationTermUnit) {
            terms.add(term);
        }
        return terms;
    }


}


