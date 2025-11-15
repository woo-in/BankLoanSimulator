package bankapp.account.service.open.loan;


import bankapp.account.model.account.LoanAccount;
import bankapp.account.repository.AccountRepository;
import bankapp.account.request.open.OpenLoanAccountRequest;
import bankapp.account.service.open.component.AccountNumberGenerator;
import bankapp.account.service.open.component.AccountOpeningValidator;
import bankapp.member.exceptions.MemberNotFoundException;
import bankapp.member.model.Member;
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
    public LoanAccount openLoanAccount(OpenLoanAccountRequest openLoanAccountRequest){

        validator.validate(openLoanAccountRequest);

        Member member = memberRepository.findById(openLoanAccountRequest.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다: " + openLoanAccountRequest.getMemberId()));


        String accountNumber = accountNumberGenerator.generate();
        LoanAccount newAccount = LoanAccount.from(openLoanAccountRequest,member,accountNumber);

        return accountRepository.save(newAccount);
    }




}
