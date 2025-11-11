package bankapp.loan.service.common.application;

import bankapp.loan.model.common.application.LoanApplication;
import bankapp.loan.web.request.LoanApplicationRequest;
import bankapp.loan.web.response.InterestRateInfoResponse;
import bankapp.loan.web.response.LoanProductInfoResponse;
import bankapp.member.model.Member;

public interface LoanApplicationService {


    LoanApplication saveLoanApplication(LoanApplicationRequest loanApplicationRequest ,
                                        LoanProductInfoResponse loanProductInfoResponse ,
                                        InterestRateInfoResponse interestRateInfoResponse ,
                                        Member loginMember);


}
