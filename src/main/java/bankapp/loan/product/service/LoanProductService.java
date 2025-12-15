package bankapp.loan.product.service;

import bankapp.loan.product.model.LoanProduct;

import java.util.List;

public interface LoanProductService {

    List<LoanProduct> findAllTypes();

}
