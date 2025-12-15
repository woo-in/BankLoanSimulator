package bankapp.loan.servicing.service;

import bankapp.account.model.account.LoanAccount;
import bankapp.loan.servicing.model.LoanRepaymentTransaction;
import java.math.BigDecimal;

public interface RepaymentTransactionService {

    LoanRepaymentTransaction createNormalTransaction(
            LoanAccount loanAccount,
            BigDecimal transactionAmount,
            BigDecimal paidScheduledInterestAmount,
            BigDecimal paidScheduledPrincipalAmount
    );

    LoanRepaymentTransaction createDelinquentTransaction(
            LoanAccount loanAccount,
            BigDecimal transactionAmount,
            BigDecimal paidPenaltyAmount,
            BigDecimal paidOverdueInterestAmount,
            BigDecimal paidOverduePrincipalAmount,
            BigDecimal paidScheduledInterestAmount,
            BigDecimal paidScheduledPrincipalAmount
    );

}
