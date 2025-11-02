package bankapp.loan.model.common.product;

import bankapp.loan.model.common.rate.LoanProductInterestRateTypeOption;
import bankapp.loan.model.common.repayment.LoanProductRepaymentOption;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
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

    @Column(name = "loan_type", insertable = false, updatable = false)
    private String loanType;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal minInterestRate;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal maxInterestRate;

    @Column(nullable = false)
    private BigDecimal maxLoanAmount;

    @Column(nullable = false)
    private Integer maxLoanTerm;

    @Embedded
    private LoanProductDetail loanProductDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "loanProduct")
    private List<LoanProductInterestRateTypeOption>  interestRateTypeOptions = new ArrayList<>();

    @OneToMany(mappedBy = "loanProduct")
    private List<LoanProductRepaymentOption>  repaymentOptions = new ArrayList<>();


}


