package bankapp.loan.product.service;


import bankapp.loan.product.model.RepaymentMethod;
import bankapp.loan.web.request.RepaymentMethodRequest;

import java.util.List;

public interface RepaymentMethodService {
    void saveRepayment(RepaymentMethod repaymentMethod);
    void saveRepayment(RepaymentMethodRequest repaymentMethodRequest);
    List<RepaymentMethod> findAllMethods();
    List<RepaymentMethod> findAllById(List<Long> ids);
}
