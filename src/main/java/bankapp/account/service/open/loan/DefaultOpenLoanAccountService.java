package bankapp.account.service.open.loan;


import bankapp.loan.servicing.model.LoanAccount;
import bankapp.account.repository.AccountRepository;
import bankapp.account.service.open.component.AccountNumberGenerator;
import bankapp.account.service.open.component.AccountOpeningValidator;
import bankapp.loan.exceptions.InvalidLoanApplication;
import bankapp.loan.underwriting.model.LoanApplication;
import bankapp.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultOpenLoanAccountService implements OpenLoanAccountService{


    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final AccountOpeningValidator validator;
    private final AccountNumberGenerator accountNumberGenerator;

    @Autowired
    public DefaultOpenLoanAccountService(MemberRepository memberRepository,
                                            AccountRepository accountRepository,
                                            AccountOpeningValidator validator,
                                            AccountNumberGenerator accountNumberGenerator) {
        this.memberRepository = memberRepository;
        this.accountRepository = accountRepository;
        this.validator = validator;
        this.accountNumberGenerator = accountNumberGenerator;
    }



    @Override
    @Transactional
    public LoanAccount openLoanAccount(LoanApplication loanApplication){

        if (loanApplication == null) {
            throw new InvalidLoanApplication("대출 신청서 정보가 없습니다.");
        }
        String newAccountNumber = accountNumberGenerator.generate();
        LoanAccount newLoanAccount = LoanAccount.from(loanApplication, newAccountNumber);
        return accountRepository.save(newLoanAccount);

    }





}
