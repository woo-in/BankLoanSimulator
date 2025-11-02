package bankapp.loan.service.common.repayment;


import bankapp.loan.model.common.repayment.RepaymentMethod;
import java.util.List;

public interface RepaymentMethodService {
    void saveRepayment(RepaymentMethod repaymentMethod);
    List<RepaymentMethod> findAllMethods();
}
