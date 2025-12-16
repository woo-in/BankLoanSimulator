package bankapp.loan.product.service;


import bankapp.loan.product.model.InterestRateType;
import bankapp.loan.product.repository.InterestRateTypeRepository;
import bankapp.loan.product.web.request.InterestRateTypeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DefaultInterestRateTypeService implements InterestRateTypeService {

    private final InterestRateTypeRepository interestRateTypeRepository;

    @Autowired
    public DefaultInterestRateTypeService(InterestRateTypeRepository interestRateTypeRepository) {
        this.interestRateTypeRepository = interestRateTypeRepository;
    }

    @Override
    @Transactional
    public void saveInterestRateType(InterestRateTypeRequest interestRateTypeRequest) {
        interestRateTypeRepository.save(interestRateTypeRequest.toEntity());
    }


    @Override
    @Transactional
    public void saveDefaultInterestRateType() {
        InterestRateType fixedRate = InterestRateType.builder()
                .typeCode("FIXED")
                .typeName("고정금리")
                .isActive(true)
                .build();

        InterestRateType variableRate = InterestRateType.builder()
                .typeCode("VARIABLE")
                .typeName("변동금리")
                .isActive(true)
                .build();

        interestRateTypeRepository.saveAll(List.of(fixedRate, variableRate));
    }


    @Override
    @Transactional(readOnly = true)
    public List<InterestRateType> findAllTypes(){
        return interestRateTypeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterestRateType> findAllById(List<Long> ids){
        return interestRateTypeRepository.findAllById(ids);
    }


}
