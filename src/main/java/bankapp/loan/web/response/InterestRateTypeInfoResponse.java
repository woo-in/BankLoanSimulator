package bankapp.loan.web.response;

import bankapp.loan.product.model.InterestRateType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class InterestRateTypeInfoResponse {

    private Long interestRateTypeId;
    private String typeCode;
    private String typeName;
    private Boolean isActive;

    private InterestRateTypeInfoResponse() { }

    /**
     * InterestRateType 엔티티를 DTO로 변환하는 정적 팩토리 메서드
     * @param entity InterestRateType 엔티티 객체
     * @return 변환된 DTO 객체
     */
    public static InterestRateTypeInfoResponse from(InterestRateType entity) {
        InterestRateTypeInfoResponse response = new InterestRateTypeInfoResponse();
        response.interestRateTypeId = entity.getInterestRateTypeId();
        response.typeCode = entity.getTypeCode();
        response.typeName = entity.getTypeName();
        response.isActive = entity.getIsActive();
        return response;
    }
}
