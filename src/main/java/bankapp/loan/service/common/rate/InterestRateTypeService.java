package bankapp.loan.service.common.rate;


import bankapp.loan.model.common.rate.InterestRateType;

import java.util.List;

public interface InterestRateTypeService {

    void saveInterestRateType(InterestRateType interestRateType);
    List<InterestRateType> findAllTypes();
}
