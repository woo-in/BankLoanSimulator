package bankapp.loan.origination.service;

import bankapp.account.model.account.LoanAccount;
import bankapp.account.request.open.OpenLoanAccountRequest;
import bankapp.account.service.open.loan.OpenLoanAccountService;
import bankapp.loan.origination.model.LoanApplication;
import bankapp.loan.underwriting.model.ContractStatus;
import bankapp.loan.underwriting.model.LoanContract;
import bankapp.loan.origination.repository.LoanContractRepository;
import bankapp.loan.origination.web.response.ExistingLoanResponse;
import bankapp.member.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class DefaultLoanContractService implements LoanContractService {

    private final LoanContractRepository loanContractRepository;
    private final OpenLoanAccountService openLoanAccountService;

    @Autowired
    public DefaultLoanContractService(LoanContractRepository loanContractRepository,
                                      OpenLoanAccountService openLoanAccountService) {
        this.loanContractRepository = loanContractRepository;
        this.openLoanAccountService = openLoanAccountService;
    }

    @Override
    @Transactional
    public LoanContract saveLoanContract(OpenLoanAccountRequest openLoanAccountRequest,
                                         LoanApplication loanApplication) {
        LoanAccount loanAccount = openLoanAccountService.openLoanAccount(openLoanAccountRequest);
        LoanContract loanContract = createLoanContractEntity(loanApplication, loanAccount);
        return loanContractRepository.save(loanContract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanContract> findAllByMember(Member member) {
        return loanContractRepository.findAllByMember(member);
    }


    @Override
    public List<ExistingLoanResponse> findAllContractResponsesByMember(Member member) {
        return loanContractRepository.findAllByMember(member).stream()
                .map(ExistingLoanResponse::from)
                .collect(Collectors.toList());
    }


    private LoanContract createLoanContractEntity(LoanApplication application, LoanAccount account) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maturityDate = calculateMaturityDate(now, application.getLoanTerm());

        LoanContract contract = LoanContract.builder()
                .loanApplication(application)
                .member(application.getMember())
                .loanProduct(application.getLoanProduct())
                .contractBaseRate(application.getAppliedBaseRate())
                .contractProductSpread(application.getAppliedProductSpread())
                .contractCreditSpread(application.getAppliedCreditSpread())
                .repaymentMethod(application.getRepaymentMethod())
                .interestRateType(application.getInterestRateType())
                .loanAmount(application.getLoanAmount())
                .loanTerm(application.getLoanTerm())
                .contractDate(now)
                .maturityDate(maturityDate)
                .status(ContractStatus.ACTIVE)
                .contractVersion(1)
                .build();

        contract.setLoanAccount(account);

        return contract;
    }
    private LocalDateTime calculateMaturityDate(LocalDateTime startDate, Integer termMonths) {
        return startDate.plusMonths(termMonths);
    }

}