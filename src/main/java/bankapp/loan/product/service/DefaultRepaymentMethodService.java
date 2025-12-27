package bankapp.loan.product.service;

import bankapp.loan.product.model.RepaymentMethod;
import bankapp.loan.product.repository.RepaymentMethodRepository;
import bankapp.loan.product.web.request.RepaymentMethodRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DefaultRepaymentMethodService implements RepaymentMethodService {


    private final RepaymentMethodRepository repaymentMethodRepository;

    @Autowired
    public DefaultRepaymentMethodService(RepaymentMethodRepository repaymentMethodRepository) {
        this.repaymentMethodRepository = repaymentMethodRepository;
    }

    @Override
    @Transactional
    public void saveRepayment(RepaymentMethodRequest repaymentMethodRequest){
        repaymentMethodRepository.save(repaymentMethodRequest.toEntity());
    }

    @Override
    @Transactional
    public void saveDefaultRepayment() {

        RepaymentMethod bullet = RepaymentMethod.builder()
                .methodCode("BULLET")
                .methodName("원금만기일시상환")
                .isActive(true)
                .build();


        RepaymentMethod equalPI = RepaymentMethod.builder()
                .methodCode("EQUAL_PRINCIPAL_INTEREST")
                .methodName("원리금균등분할상환")
                .isActive(true)
                .build();

        RepaymentMethod equalP = RepaymentMethod.builder()
                .methodCode("EQUAL_PRINCIPAL")
                .methodName("원금균등분할상환")
                .isActive(true)
                .build();

        repaymentMethodRepository.saveAll(List.of(bullet, equalPI, equalP));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentMethod> findAllMethods() {
        return repaymentMethodRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentMethod> findAllById(List<Long> ids){
        return repaymentMethodRepository.findAllById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public RepaymentMethod findByMethodName(String methodName) {
        return repaymentMethodRepository.findByMethodName(methodName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상환 방법입니다: " + methodName));
    }



}
