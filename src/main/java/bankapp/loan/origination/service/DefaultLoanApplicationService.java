package bankapp.loan.origination.service;

import bankapp.loan.exceptions.InvalidInterestRate;
import bankapp.loan.exceptions.InvalidLoanApplication;
import bankapp.loan.exceptions.InvalidLoanProduct;
import bankapp.loan.exceptions.InvalidRepaymentMethodId;
import bankapp.loan.origination.model.ApplicationStatus;
import bankapp.loan.origination.model.LoanApplication;
import bankapp.loan.product.model.LoanProduct;
import bankapp.loan.product.model.InterestRateType;
import bankapp.loan.product.model.RepaymentMethod;
import bankapp.loan.origination.repository.LoanApplicationRepository;
import bankapp.loan.product.repository.LoanProductRepository;
import bankapp.loan.product.repository.InterestRateTypeRepository;
import bankapp.loan.product.repository.RepaymentMethodRepository;
import bankapp.loan.web.request.LoanApplicationRequest;
import bankapp.loan.origination.web.response.InterestRateInfoResponse;
import bankapp.loan.product.web.response.LoanProductInfoResponse;
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
    public LoanApplication saveLoanApplication(LoanApplicationRequest request,
                                               LoanProductInfoResponse productInfo,
                                               InterestRateInfoResponse rateInfo,
                                               Member loginMember) {

        // todo : controller 부터 뭔가 꼬였다.
        LoanProduct loanProduct = findLoanProduct(productInfo.getLoanProductSlug());
        RepaymentMethod repaymentMethod = findRepaymentMethod(request.getRepaymentMethodId());
        InterestRateType interestRateType = findInterestRateType(request.getInterestRateTypeId());

        LoanApplication loanApplication = createApplicationEntity(
                loginMember, loanProduct, repaymentMethod, interestRateType, request, rateInfo
        );

        return loanApplicationRepository.save(loanApplication);
    }



    @Override
    @Transactional
    public List<LoanApplication> getAppliedApplications() {
        return loanApplicationRepository.findByApplicationStatusOrderByCreatedAtDesc(ApplicationStatus.APPLIED);
    }

    @Override
    @Transactional
    public void rejectApplication(Long applicationId) {
        LoanApplication application = findAndValidateApplication(applicationId);
        application.setApplicationStatus(ApplicationStatus.REJECTED);
    }

    @Override
    @Transactional
    public void approveApplication(Long applicationId) {
        LoanApplication application = findAndValidateApplication(applicationId);
        application.setApplicationStatus(ApplicationStatus.APPROVED);
    }

    @Override
    @Transactional
    public Optional<LoanApplication> findById(Long applicationId){
        return loanApplicationRepository.findById(applicationId);
    }



    private LoanProduct findLoanProduct(String slug) {
        return loanProductRepository.findByLoanProductSlug(slug)
                .orElseThrow(() -> new InvalidLoanProduct("대출 상품 정보를 찾을 수 없습니다. slug: " + slug));
    }

    private RepaymentMethod findRepaymentMethod(Long id) {
        return repaymentMethodRepository.findById(id)
                .orElseThrow(() -> new InvalidRepaymentMethodId("상환 방법 정보를 찾을 수 없습니다. ID: " + id));
    }

    private InterestRateType findInterestRateType(Long id) {
        return interestRateTypeRepository.findById(id)
                .orElseThrow(() -> new InvalidInterestRate("금리 유형 정보를 찾을 수 없습니다. ID: " + id));
    }

    private LoanApplication createApplicationEntity(Member member,
                                                    LoanProduct product,
                                                    RepaymentMethod method,
                                                    InterestRateType rateType,
                                                    LoanApplicationRequest request,
                                                    InterestRateInfoResponse rateInfo) {
        LoanApplication application = new LoanApplication();

        application.setMember(member);
        application.setLoanProduct(product);
        application.setRepaymentMethod(method);
        application.setInterestRateType(rateType);
        application.setLoanAmount(request.getLoanAmount());
        application.setLoanTerm(request.getLoanTerm());
        application.setAppliedBaseRate(rateInfo.getBaseRate());
        application.setAppliedCreditSpread(rateInfo.getCreditSpread());
        application.setAppliedProductSpread(rateInfo.getProductSpread());

        application.setApplicationStatus(ApplicationStatus.APPLIED);

        return application;
    }


    private LoanApplication findAndValidateApplication(Long applicationId) {

        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new InvalidLoanApplication("해당 대출 신청을 찾을 수 없습니다. ID: " + applicationId));

        if (application.getApplicationStatus() != ApplicationStatus.APPLIED) {
            throw new IllegalStateException("이미 처리된 대출 신청입니다. (현재 상태: " + application.getApplicationStatus() + ")");
        }

        return application;
    }



}