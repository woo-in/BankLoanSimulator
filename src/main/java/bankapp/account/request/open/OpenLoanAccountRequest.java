package bankapp.account.request.open;

import bankapp.account.model.account.Account;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class OpenLoanAccountRequest {

    private Long memberId;
    private BigDecimal balance;
    private String nickname;
    private Account repaymentAccount;

}
