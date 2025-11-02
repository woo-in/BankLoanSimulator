package bankapp.loan.service.common.repayment;

import bankapp.loan.model.common.repayment.RepaymentMethod;
import bankapp.loan.repository.common.repayment.RepaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DefaultRepaymentMethodService implements RepaymentMethodService {


    private final RepaymentMethodRepository repaymentMethodRepository;

    @Autowired
    public DefaultRepaymentMethodService(RepaymentMethodRepository repaymentMethodRepository) {
        this.repaymentMethodRepository = repaymentMethodRepository;
    }

    @Override
    public void saveRepayment(RepaymentMethod repaymentMethod){
        repaymentMethodRepository.save(repaymentMethod);
    }

    @Override
    public List<RepaymentMethod> findAllMethods() {
        return repaymentMethodRepository.findAll();
    }

}
