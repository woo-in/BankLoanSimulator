package bankapp.loan.origination.service;

import bankapp.loan.origination.model.LoanApplication;
import bankapp.loan.web.request.LoanApplicationRequest;
import bankapp.loan.web.response.InterestRateInfoResponse;
import bankapp.loan.web.response.LoanProductInfoResponse;
import bankapp.member.model.Member;
import java.util.List;
import java.util.Optional;

public interface LoanApplicationService {

    LoanApplication saveLoanApplication(LoanApplicationRequest loanApplicationRequest ,
                                        LoanProductInfoResponse loanProductInfoResponse ,
                                        InterestRateInfoResponse interestRateInfoResponse ,
                                        Member loginMember);

    List<LoanApplication> getAppliedApplications() ;

    void rejectApplication(Long applicationId);
    void approveApplication(Long applicationId);

    Optional<LoanApplication> findById(Long applicationId);

}
