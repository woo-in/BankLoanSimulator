package bankapp.loan.model.common.repayment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentMethod {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repaymentMethodId;

    @Column(unique = true, nullable = false, length = 50)
    private String methodCode;

    @Column(nullable = false, length = 100)
    private String methodName;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "repaymentMethod")
    @Builder.Default
    private List<LoanProductRepaymentOption> loanProductRepaymentOptions = new ArrayList<>();


}
