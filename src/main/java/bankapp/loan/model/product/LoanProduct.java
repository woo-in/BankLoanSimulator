package bankapp.loan.model.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;


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


//    /**
//     * 이 상품이 가질 수 있는 상환 방법 옵션 목록.
//     * 이 상품(LoanProduct)이 저장될 때(persist),
//     * 이 리스트에 담긴 LoanProductRepaymentOption 객체들도 함께 저장됩니다. (cascade = CascadeType.ALL)
//     */
//    @OneToMany(mappedBy = "loanProduct", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    private List<LoanProductRepaymentOption> repaymentOptions = new ArrayList<>();
//
//
//    public void addRepaymentOption(RepaymentMethod method) {
//        LoanProductRepaymentOption option = new LoanProductRepaymentOption(this, method);
//        this.repaymentOptions.add(option);
//    }




}


