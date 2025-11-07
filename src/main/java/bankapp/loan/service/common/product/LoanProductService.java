package bankapp.loan.service.common.product;

import bankapp.loan.model.common.product.LoanProduct;

import java.util.List;

public interface LoanProductService {

    List<LoanProduct> findAllTypes();

}
