package bankapp.account.service.open.loan;

import bankapp.account.model.account.LoanAccount;
import bankapp.account.request.open.OpenLoanAccountRequest;

public interface OpenLoanAccountService {

    LoanAccount openLoanAccount(OpenLoanAccountRequest openLoanAccountRequest);

}
