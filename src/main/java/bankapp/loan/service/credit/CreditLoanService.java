package bankapp.loan.service.credit;

import bankapp.loan.common.enums.FinancialGrade;
import bankapp.loan.model.credit.CreditLoanProduct;
import bankapp.loan.web.request.LoanProductRequest;
import bankapp.loan.web.response.InterestRateInfoResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CreditLoanService {

    List<CreditLoanProduct> findAllCreditLoanProducts();
    void saveCreditLoanProduct(LoanProductRequest loanProductRequest);
    CreditLoanProduct findCreditLoanProductByLoanProductSlug(String loanProductSlug);
    InterestRateInfoResponse calculateInterestRate(FinancialGrade financialGrade , String loanProductSlug) ;




}
