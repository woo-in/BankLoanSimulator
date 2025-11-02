package bankapp.loan.service.common.rate;


import bankapp.loan.model.common.rate.InterestRateType;
import bankapp.loan.repository.common.rate.InterestRateTypeRepository;
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



}
