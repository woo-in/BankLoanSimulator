package bankapp.loan.product.service;

import bankapp.loan.exceptions.InvalidLoanProduct;
import bankapp.loan.product.model.LoanProduct;
import bankapp.loan.product.repository.LoanProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DefaultLoanProductService implements LoanProductService {


    private final LoanProductRepository loanProductRepository;


    @Autowired
    public DefaultLoanProductService(LoanProductRepository loanProductRepository) {
        this.loanProductRepository = loanProductRepository;
    }

    @Override
    public List<LoanProduct> findAllTypes(){
        return loanProductRepository.findAll();
    }


    @Override
    @Transactional(readOnly = true)
    public LoanProduct findByLoanProductSlug(String slug) {
        return loanProductRepository.findByLoanProductSlug(slug)
                .orElseThrow(() -> new InvalidLoanProduct("존재하지 않는 상품입니다: " + slug));
    }



}
