package bankapp.loan.service.common.rate;


import bankapp.loan.model.common.rate.InterestRateType;
import bankapp.loan.web.request.InterestRateTypeRequest;

import java.util.List;

public interface InterestRateTypeService {

    void saveInterestRateType(InterestRateType interestRateType);
    void saveInterestRateType(InterestRateTypeRequest interestRateTypeRequest);
    List<InterestRateType> findAllTypes();
    List<InterestRateType> findAllById(List<Long> ids);
}
