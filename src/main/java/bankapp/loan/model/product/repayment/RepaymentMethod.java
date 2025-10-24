package bankapp.loan.model.product.repayment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class RepaymentMethod {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repaymentMethodId;

    @Column(unique = true, nullable = false, length = 50)
    private String methodCode;

    @Column(nullable = false, length = 100)
    private String methodName;

    @Column(nullable = false)
    private Boolean isActive = true;



}
