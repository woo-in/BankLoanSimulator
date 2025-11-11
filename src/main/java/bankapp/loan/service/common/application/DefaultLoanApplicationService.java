package bankapp.loan.service.common.application;

import bankapp.loan.exceptions.InvalidInterestRate;
import bankapp.loan.exceptions.InvalidLoanProduct;
import bankapp.loan.exceptions.InvalidRepaymentMethodId;
import bankapp.loan.model.common.application.ApplicationStatus;
import bankapp.loan.model.common.application.LoanApplication;
import bankapp.loan.model.common.product.LoanProduct;
import bankapp.loan.model.common.rate.InterestRateType;
import bankapp.loan.model.common.repayment.RepaymentMethod;
import bankapp.loan.repository.common.application.LoanApplicationRepository;
import bankapp.loan.repository.common.product.LoanProductRepository;
import bankapp.loan.repository.common.rate.InterestRateTypeRepository;
import bankapp.loan.repository.common.repayment.RepaymentMethodRepository;
import bankapp.loan.web.request.LoanApplicationRequest;
import bankapp.loan.web.response.InterestRateInfoResponse;
import bankapp.loan.web.response.LoanProductInfoResponse;
import bankapp.member.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultLoanApplicationService implements LoanApplicationService {


    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanProductRepository loanProductRepository;
    private final RepaymentMethodRepository repaymentMethodRepository;
    private final InterestRateTypeRepository interestRateTypeRepository;

    @Autowired
    public DefaultLoanApplicationService(LoanApplicationRepository loanApplicationRepository,
                                         LoanProductRepository loanProductRepository,
                                         RepaymentMethodRepository repaymentMethodRepository,
                                         InterestRateTypeRepository interestRateTypeRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanProductRepository = loanProductRepository;
        this.repaymentMethodRepository = repaymentMethodRepository;
        this.interestRateTypeRepository = interestRateTypeRepository;
    }

    @Override
    @Transactional
    public LoanApplication saveLoanApplication(LoanApplicationRequest loanApplicationRequest ,
                                               LoanProductInfoResponse loanProductInfoResponse ,
                                               InterestRateInfoResponse interestRateInfoResponse ,
                                               Member loginMember){

        LoanProduct loanProduct = loanProductRepository.findByLoanProductSlug(loanProductInfoResponse.getLoanProductSlug())
                .orElseThrow(() -> new InvalidLoanProduct("대출 상품 정보를 찾을 수 없습니다."));

        RepaymentMethod repaymentMethod = repaymentMethodRepository.findById(loanApplicationRequest.getRepaymentMethodId())
                .orElseThrow(() -> new InvalidRepaymentMethodId("상환 방법 정보를 찾을 수 없습니다."));

        InterestRateType interestRateType = interestRateTypeRepository.findById(loanApplicationRequest.getInterestRateTypeId())
                .orElseThrow(() -> new InvalidInterestRate("금리 유형 정보를 찾을 수 없습니다."));


        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setMember(loginMember);
        loanApplication.setLoanProduct(loanProduct);
        loanApplication.setRepaymentMethod(repaymentMethod);
        loanApplication.setInterestRateType(interestRateType);
        loanApplication.setLoanAmount(loanApplicationRequest.getLoanAmount());
        loanApplication.setLoanTerm(loanApplicationRequest.getLoanTerm());
        loanApplication.setAppliedRate(interestRateInfoResponse.getFinalInterestRate());
        loanApplication.setApplicationStatus(ApplicationStatus.APPLIED);

        return loanApplicationRepository.save(loanApplication);





    }



}
