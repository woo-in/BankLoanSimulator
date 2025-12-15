package bankapp.loan.product.service;

import bankapp.loan.product.model.RepaymentMethod;
import bankapp.loan.product.repository.RepaymentMethodRepository;
import bankapp.loan.web.request.RepaymentMethodRequest;
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
    public void saveRepayment(RepaymentMethodRequest repaymentMethodRequest){
        repaymentMethodRepository.save(repaymentMethodRequest.toEntity());
    }

    @Override
    public List<RepaymentMethod> findAllMethods() {
        return repaymentMethodRepository.findAll();
    }

    @Override
    public List<RepaymentMethod> findAllById(List<Long> ids){
        return repaymentMethodRepository.findAllById(ids);
    }

}
