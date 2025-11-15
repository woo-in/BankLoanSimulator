package bankapp.loan.service.common.contract;

import bankapp.account.request.open.OpenLoanAccountRequest;
import bankapp.loan.model.common.application.LoanApplication;
import bankapp.loan.model.common.contract.LoanContract;

public interface LoanContractService {

    LoanContract saveLoanContract(OpenLoanAccountRequest openLoanAccountRequest ,
                                         LoanApplication loanApplication);

}
