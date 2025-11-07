package bankapp.loan.service.common.product;

import bankapp.loan.model.common.product.LoanProduct;
import bankapp.loan.repository.common.product.LoanProductRepository;
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
