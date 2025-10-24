package bankapp.loan.model.product.rate;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class InterestRateType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interestRateTypeId;

    @Column(unique = true, nullable = false, length = 50)
    private String typeCode;

    @Column(nullable = false, length = 100)
    private String typeName;

    @Column(nullable = false)
    private Boolean isActive = true;


}
