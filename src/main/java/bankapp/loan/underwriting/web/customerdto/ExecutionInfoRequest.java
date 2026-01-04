package bankapp.loan.underwriting.web.customerdto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ExecutionInfoRequest {

    /**
     * 상환(자동이체) 계좌 ID
     * - 화면의 Select Box에서 선택된 Account ID
     */
    @NotNull(message = "상환 계좌를 선택해주세요.")
    private Long repaymentAccountId;

    /**
     * 대출금 수령 계좌 ID
     * - 화면의 Select Box에서 선택된 Account ID
     */
    @NotNull(message = "대출금을 수령할 계좌를 선택해주세요.")
    private Long disbursementAccountId;

    /**
     * 희망 결제일
     * - 2월 및 휴일 처리를 고려하여 보통 28일까지만 허용하는 것이 안전함
     */
    @NotNull(message = "결제일을 선택해주세요.")
    @Min(value = 1, message = "결제일은 1일 이상이어야 합니다.")
    @Max(value = 28, message = "결제일은 매월 28일 이전으로 설정해야 합니다.")
    private Integer paymentDay;

}
