package bankapp.loan.underwriting.service;

import bankapp.account.model.account.LoanAccount;
import bankapp.account.request.account.AccountTransactionRequest;
import bankapp.account.service.account.AccountService;
import bankapp.account.service.check.AccountCheckService;
import bankapp.account.service.open.loan.OpenLoanAccountService;
import bankapp.loan.exceptions.LoanApplicationNotFoundException;
import bankapp.loan.servicing.service.LoanRepaymentService;
import bankapp.loan.underwriting.model.ApplicationStatus;
import bankapp.loan.underwriting.model.LoanApplication;
import bankapp.loan.origination.model.PendingLoanApplication;
import bankapp.loan.underwriting.model.LoanContract;
import bankapp.loan.underwriting.repository.LoanApplicationRepository;
import bankapp.loan.underwriting.web.customerdto.ContractAuthRequest;
import bankapp.loan.underwriting.web.customerdto.ExecutionInfoRequest;
import bankapp.loan.underwriting.web.request.ApprovedLoanApplicationDto;
import bankapp.loan.underwriting.web.request.RejectedLoanApplicationDto;
import bankapp.member.exceptions.IncorrectPasswordException;
import bankapp.member.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class DefaultLoanApplicationService implements LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final AccountService accountService;
    private final AccountCheckService accountCheckService;
    private final OpenLoanAccountService openLoanAccountService;
    private final LoanContractService loanContractService;
    private final LoanRepaymentService loanRepaymentService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DefaultLoanApplicationService(LoanApplicationRepository loanApplicationRepository,
                                         AccountCheckService accountCheckService,
                                         AccountService accountService,
                                         OpenLoanAccountService openLoanAccountService,
                                         LoanContractService loanContractService,
                                         PasswordEncoder passwordEncoder,
                                         LoanRepaymentService loanRepaymentService) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.accountCheckService = accountCheckService;
        this.accountService = accountService;
        this.openLoanAccountService = openLoanAccountService;
        this.loanContractService = loanContractService;
        this.loanRepaymentService = loanRepaymentService;
        this.passwordEncoder = passwordEncoder;
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
    public List<LoanApplication> getLoanApplicationsByMemberId(Long memberId) {
        return loanApplicationRepository.findAllByMember_MemberIdOrderByCreatedAtDesc(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplication getLoanApplicationById(long applicationId){
        return loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("대출 신청서를 찾을 수 없습니다."));

    }


    @Override
    @Transactional
    public void approveApplication(Long applicationId , ApprovedLoanApplicationDto approvedLoanApplicationDto) {
        LoanApplication application = findAppliedApplication(applicationId);

        application.setApprovedLoanAmount(approvedLoanApplicationDto.getApprovedLoanAmount());
        application.setApprovedLoanTerm(approvedLoanApplicationDto.getApprovedLoanTerm());
        application.setApprovedBaseRate(approvedLoanApplicationDto.getApprovedBaseRate());
        application.setApprovedProductSpread(approvedLoanApplicationDto.getApprovedProductSpread());
        application.setApprovedCreditSpread(approvedLoanApplicationDto.getApprovedCreditSpread());
        application.setApprovedSelectionSpread(approvedLoanApplicationDto.getApprovedSelectionSpread());
        application.setApprovedFinalInterestRate(approvedLoanApplicationDto.getApprovedFinalInterestRate());
        application.setApprovedDebtServiceRatio(approvedLoanApplicationDto.getCalculatedDsr());
        application.setMessageToCustomer(approvedLoanApplicationDto.getMessageToCustomer());

        application.setApplicationStatus(ApplicationStatus.APPROVED);

    }

    @Override
    @Transactional
    public void rejectApplication(Long applicationId , RejectedLoanApplicationDto rejectedLoanApplicationDto) {
        LoanApplication application = findAppliedApplication(applicationId);
        application.setApplicationStatus(ApplicationStatus.REJECTED);
        application.setMessageToCustomer(rejectedLoanApplicationDto.getMessageToCustomer());
    }

    @Override
    @Transactional
    public void cancelApplication(Long applicationId){
        LoanApplication application = findApprovedApplication(applicationId);
        application.setApplicationStatus(ApplicationStatus.CANCELED);
    }

    @Override
    @Transactional
    public void updateApplicationExecutionInfo(Long applicationId , ExecutionInfoRequest executionInfoRequest) {
        LoanApplication application = findApprovedApplication(applicationId);
        application.setDisbursementAccount(accountCheckService.findAccountByAccountId(executionInfoRequest.getDisbursementAccountId()));
        application.setRepaymentAccount(accountCheckService.findAccountByAccountId(executionInfoRequest.getRepaymentAccountId()));
        application.setPaymentDay(executionInfoRequest.getPaymentDay());
    }

    @Override
    @Transactional
    public void signContract(Long applicationId , Member loginMember , ContractAuthRequest contractAuthRequest){

        LoanApplication application = findApprovedApplication(applicationId);

        // 비밀번호 확인
        if(!passwordEncoder.matches(contractAuthRequest.getPassword(), loginMember.getPassword())) {
            throw new IncorrectPasswordException("비밀번호가 일치하지 않습니다.");
        }

        // 대출계좌 생성
        LoanAccount loanAccount = openLoanAccountService.openLoanAccount(application);

        // 대출계약서 작성
        LoanContract loanContract = loanContractService.saveLoanContract(application , loanAccount);

        // 스케줄러 작성
        loanRepaymentService.saveRepaymentSchedule(loanAccount , loanContract);

        // 대출금 입금 진행
        AccountTransactionRequest debitTransaction =
                new AccountTransactionRequest(Long.parseLong("1"),application.getLoanAmount(),"대출 출금");
        accountService.debit(debitTransaction);

        AccountTransactionRequest creditTransaction =
                new AccountTransactionRequest(application.getDisbursementAccount().getAccountId(),application.getLoanAmount(),"대출 입금");
        accountService.credit(creditTransaction);

        // 신청서 상태 변경
        application.setApplicationStatus(ApplicationStatus.CONTRACTED);
    }

    @Override
    @Transactional
    public Optional<LoanApplication> findById(Long applicationId){
        return loanApplicationRepository.findById(applicationId);
    }

    private LoanApplication findAppliedApplication(Long applicationId) {
        return loanApplicationRepository.findByLoanApplicationIdAndApplicationStatus(applicationId, ApplicationStatus.APPLIED)
                .orElseThrow(() -> new LoanApplicationNotFoundException("대출 신청 내역이 존재하지 않거나, 심사 대기 상태가 아닙니다. (ID: " + applicationId + ")"));
    }

    private LoanApplication findApprovedApplication(Long applicationId) {
        return loanApplicationRepository.findByLoanApplicationIdAndApplicationStatus(applicationId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new LoanApplicationNotFoundException("승인된 대출 내역을 찾을 수 없습니다. (ID: " + applicationId + ")"));
    }







}