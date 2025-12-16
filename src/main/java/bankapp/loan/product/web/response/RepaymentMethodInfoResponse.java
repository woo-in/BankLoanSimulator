package bankapp.loan.product.web.response;

import bankapp.loan.product.model.RepaymentMethod;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class RepaymentMethodInfoResponse {

    private Long repaymentMethodId;
    private String methodCode;
    private String methodName;
    private Boolean isActive;

    /**
     * 외부에서의 직접 생성을 막기 위한 private 생성자
     */
    private RepaymentMethodInfoResponse() { }

    /**
     * RepaymentMethod 엔티티를 DTO로 변환하는 정적 팩토리 메서드
     * @param entity RepaymentMethod 엔티티 객체
     * @return 변환된 DTO 객체
     */
    public static RepaymentMethodInfoResponse from(RepaymentMethod entity) {
        RepaymentMethodInfoResponse response = new RepaymentMethodInfoResponse();
        response.repaymentMethodId = entity.getRepaymentMethodId();
        response.methodCode = entity.getMethodCode();
        response.methodName = entity.getMethodName();
        response.isActive = entity.getIsActive();
        return response;
    }

}
