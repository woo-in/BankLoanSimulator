package bankapp.loan.servicing.service.lifecycle;

import bankapp.loan.servicing.model.LoanAccount;
import bankapp.loan.servicing.model.LoanStatus;

public interface LoanStatusService {

    void changeLoanStatus(LoanAccount loanAccount , LoanStatus targetStatus);
}
