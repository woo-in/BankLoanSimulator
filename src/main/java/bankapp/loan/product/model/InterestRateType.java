package bankapp.loan.product.model;

import bankapp.loan.product.enums.InterestRateTypeEnum;
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

    // todo : 혼합형 고려 (고정 -> 변동)
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

    public InterestRateTypeEnum getTypeEnum() {
        try {
            return InterestRateTypeEnum.valueOf(this.typeCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalStateException("지원하지 않는 이자 종류 코드입니다: " + this.typeCode);
        }
    }


}
