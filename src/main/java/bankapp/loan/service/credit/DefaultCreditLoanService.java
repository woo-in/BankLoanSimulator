package bankapp.loan.service.credit;

import bankapp.loan.exceptions.LoanProductNotFoundException;
import bankapp.loan.model.credit.CreditLoanProduct;
import bankapp.loan.repository.credit.CreditLoanProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DefaultCreditLoanService implements CreditLoanService {


    private final CreditLoanProductRepository creditLoanProductRepository;

    @Autowired
    public DefaultCreditLoanService(CreditLoanProductRepository creditLoanProductRepository) {
        this.creditLoanProductRepository = creditLoanProductRepository;
    }

    @Override
    public List<CreditLoanProduct> findAllCreditLoanProducts() {
        return creditLoanProductRepository.findAll();
    }

    @Override
    public void saveCreditLoanProduct(CreditLoanProduct creditLoanProduct) {
        creditLoanProductRepository.save(creditLoanProduct);
    }


    @Override
    public CreditLoanProduct findCreditLoanProductByLoanProductSlug(String loanProductSlug) throws LoanProductNotFoundException {

        return creditLoanProductRepository.findByLoanProductSlug(loanProductSlug)
                .orElseThrow(() -> new LoanProductNotFoundException("해당 신용대출 상품을 찾을 수 없습니다 : " + loanProductSlug));
    }



}
