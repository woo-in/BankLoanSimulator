package bankapp.loan.product.service;

import bankapp.loan.product.model.LoanProduct;
import bankapp.loan.product.repository.LoanProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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


}
