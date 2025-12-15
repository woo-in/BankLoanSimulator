package bankapp.loan.origination.service;

import bankapp.account.request.open.OpenLoanAccountRequest;
import bankapp.loan.origination.model.LoanApplication;
import bankapp.loan.origination.model.LoanContract;

public interface LoanContractService {

    LoanContract saveLoanContract(OpenLoanAccountRequest openLoanAccountRequest ,
                                         LoanApplication loanApplication);

}
