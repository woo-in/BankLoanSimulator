package bankapp.loan.service.common.application;

import bankapp.loan.exceptions.InvalidInterestRate;
import bankapp.loan.exceptions.InvalidLoanApplication;
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

import java.util.List;
import java.util.Optional;

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
        loanApplication.setAppliedBaseRate(interestRateInfoResponse.getBaseRate());
        loanApplication.setAppliedCreditSpread(interestRateInfoResponse.getCreditSpread());
        loanApplication.setAppliedProductSpread(interestRateInfoResponse.getProductSpread());

        loanApplication.setApplicationStatus(ApplicationStatus.APPLIED);

        return loanApplicationRepository.save(loanApplication);
    }

    @Override
    @Transactional
    public List<LoanApplication> getAppliedApplications() {
        // APPLIED 상태인 것만 최신순으로 가져오기
        return loanApplicationRepository.findByApplicationStatusOrderByCreatedAtDesc(ApplicationStatus.APPLIED);
    }


    @Override
    @Transactional
    public void rejectApplication(Long applicationId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new InvalidLoanApplication("해당 대출 신청을 찾을 수 없습니다. ID: " + applicationId));

        if (application.getApplicationStatus() != ApplicationStatus.APPLIED) {
            throw new IllegalStateException("이미 처리된 대출 신청입니다.");
        }

        application.setApplicationStatus(ApplicationStatus.REJECTED);
    }

    @Override
    @Transactional
    public void approveApplication(Long applicationId){
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new InvalidLoanApplication("해당 대출 신청을 찾을 수 없습니다. ID: " + applicationId));

        if (application.getApplicationStatus() != ApplicationStatus.APPLIED) {
            throw new IllegalStateException("이미 처리된 대출 신청입니다.");
        }

        application.setApplicationStatus(ApplicationStatus.APPROVED);
    }

    @Override
    @Transactional
    public Optional<LoanApplication> findById(Long applicationId){
        return loanApplicationRepository.findById(applicationId);
    }


}
