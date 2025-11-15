package bankapp.account.request.open;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class OpenLoanAccountRequest {

    private Long memberId;
    private BigDecimal balance;
    private String nickname;

}
