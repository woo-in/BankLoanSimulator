package bankapp.loan.servicing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentDetail {
    private int sequence; // 회차 정보
    private BigDecimal principal;  // 계산된 원금
    private BigDecimal interest;    // 계산된 이자
}

