package bankapp.loan.model.common.rate;

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

    @OneToMany(mappedBy = "interestRateType")
    private List<LoanProductInterestRateTypeOption> interestRateTypeOptions = new ArrayList<>();


}
