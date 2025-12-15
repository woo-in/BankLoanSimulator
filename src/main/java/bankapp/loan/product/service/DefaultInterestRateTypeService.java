package bankapp.loan.product.service;


import bankapp.loan.product.model.InterestRateType;
import bankapp.loan.product.repository.InterestRateTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultInterestRateTypeService implements InterestRateTypeService {

    private final InterestRateTypeRepository interestRateTypeRepository;

    @Autowired
    public DefaultInterestRateTypeService(InterestRateTypeRepository interestRateTypeRepository) {
        this.interestRateTypeRepository = interestRateTypeRepository;
    }

    @Override
    public void saveInterestRateType(InterestRateType interestRateType) {
        interestRateTypeRepository.save(interestRateType);
    }

    @Override
    public List<InterestRateType> findAllTypes(){
        return interestRateTypeRepository.findAll();
    }



    @Override
    public List<InterestRateType> findAllById(List<Long> ids){
        return interestRateTypeRepository.findAllById(ids);
    }


}
