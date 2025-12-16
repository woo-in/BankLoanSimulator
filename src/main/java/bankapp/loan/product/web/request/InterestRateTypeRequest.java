package bankapp.loan.product.web.request;

import bankapp.loan.product.model.InterestRateType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InterestRateTypeRequest {

    private String typeCode;
    private String typeName;
    private Boolean isActive;

    /**
     * DTO의 데이터를 기반으로 InterestRateType 엔티티 객체를 생성합니다.
     * @return InterestRateType 엔티티
     */
    public InterestRateType toEntity() {
        return InterestRateType.builder()
                .typeCode(this.typeCode)
                .typeName(this.typeName)
                .isActive(this.isActive != null && this.isActive)
                .build();
    }

}
