package bankapp.account.service.open.loan;

import bankapp.loan.servicing.model.LoanAccount;
import bankapp.loan.underwriting.model.LoanApplication;

public interface OpenLoanAccountService {

    // 대출 계좌 생성
    LoanAccount openLoanAccount(LoanApplication loanApplication);
}
