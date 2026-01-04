package bankapp.account.service.open.loan;

import bankapp.account.model.account.LoanAccount;
import bankapp.loan.underwriting.model.LoanApplication;

public interface OpenLoanAccountService {

    LoanAccount openLoanAccount(LoanApplication loanApplication);
}
