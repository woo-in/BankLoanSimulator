package bankapp.loan.web.request;


import bankapp.loan.model.common.repayment.RepaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RepaymentMethodRequest {

    private String methodCode;
    private String methodName;
    private Boolean isActive; // Checkbox는 Boolean 객체 타입이 편리

    /**
     * DTO의 데이터를 기반으로 RepaymentMethod 엔티티 객체를 생성합니다.
     * @return RepaymentMethod 엔티티
     */
    public RepaymentMethod toEntity() {
        return RepaymentMethod.builder()
                .methodCode(this.methodCode)
                .methodName(this.methodName)
                .isActive(this.isActive != null && this.isActive)
                .build();
    }

}
