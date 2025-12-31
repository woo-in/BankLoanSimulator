package bankapp.loan.underwriting.service;

import bankapp.loan.exceptions.InvalidLoanApplication;
import bankapp.loan.exceptions.LoanApplicationNotFoundException;
import bankapp.loan.underwriting.model.ApplicationStatus;
import bankapp.loan.underwriting.model.LoanApplication;
import bankapp.loan.origination.model.PendingLoanApplication;
import bankapp.loan.underwriting.repository.LoanApplicationRepository;
import bankapp.member.exceptions.MemberNotFoundException;
import bankapp.member.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class DefaultLoanApplicationService implements LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;

    @Autowired
    public DefaultLoanApplicationService(LoanApplicationRepository loanApplicationRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
    }



    @Override
    @Transactional
    public void saveLoanApplication(PendingLoanApplication pendingLoanApplication){
        LoanApplication newApplication = LoanApplication.createFrom(pendingLoanApplication);
        loanApplicationRepository.save(newApplication);
    }


    @Override
    @Transactional(readOnly = true)
    public List<LoanApplication> getAppliedApplications() {
        return loanApplicationRepository.findByApplicationStatusOrderByCreatedAtDesc(ApplicationStatus.APPLIED);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplication getLoanApplicationById(long applicationId){
        return loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("대출 신청서를 찾을 수 없습니다."));

    }

    @Override
    @Transactional
    public void rejectApplication(Long applicationId , String reason) {
        LoanApplication application = findAndValidateApplication(applicationId);
        application.setApplicationStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(reason);
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


    private LoanApplication findAndValidateApplication(Long applicationId) {

        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new InvalidLoanApplication("해당 대출 신청을 찾을 수 없습니다. ID: " + applicationId));

        if (application.getApplicationStatus() != ApplicationStatus.APPLIED) {
            throw new IllegalStateException("이미 처리된 대출 신청입니다. (현재 상태: " + application.getApplicationStatus() + ")");
        }

        return application;
    }



}