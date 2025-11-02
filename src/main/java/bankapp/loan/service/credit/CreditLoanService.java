package bankapp.loan.service.credit;

import bankapp.loan.model.credit.CreditLoanProduct;

import java.util.List;

public interface CreditLoanService {

    List<CreditLoanProduct> findAllCreditLoanProducts();
    void saveCreditLoanProduct(CreditLoanProduct creditLoanProduct);

    CreditLoanProduct findCreditLoanProductByLoanProductSlug(String loanProductSlug);

}
