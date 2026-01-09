package bankapp.loan.servicing.component;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentDetail {
    private LocalDate dueDate; // 납부 예정일
    private BigDecimal principal;  // 계산된 원금
    private BigDecimal interest;    // 계산된 이자
}