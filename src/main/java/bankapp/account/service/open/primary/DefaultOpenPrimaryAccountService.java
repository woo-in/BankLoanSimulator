package bankapp.account.service.open.primary;

import bankapp.account.model.account.PrimaryAccount;
import bankapp.account.repository.AccountRepository;
import bankapp.account.request.open.OpenPrimaryAccountRequest;
import bankapp.account.exceptions.InvalidDepositAmountException;
import bankapp.account.service.open.component.AccountNumberGenerator;
import bankapp.account.service.open.component.AccountOpeningValidator;
import bankapp.member.exceptions.MemberNotFoundException;
import bankapp.member.model.Member;
import bankapp.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


/**
 * {@inheritDoc}
 * <p>
 * 이 구현체는 AccountDao와 MemberCheckService와 협력하여,
 * 회원 존재 여부와 초기 입금액을 검증한 후, 새로운 계좌번호를 생성하여
 * 데이터베이스에 신규 주계좌 정보를 저장하는 방식으로 로직을 수행합니다.
 */

@Service
public class DefaultOpenPrimaryAccountService implements OpenPrimaryAccountService{

    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final AccountOpeningValidator validator;
    private final AccountNumberGenerator accountNumberGenerator;

    private static final BigDecimal CORE_INITIAL_BALANCE = new BigDecimal("200000000000000");
    private static final String CORE_ACCOUNT_NICKNAME = "우인은행 중앙 금고";


    @Autowired
    public DefaultOpenPrimaryAccountService(MemberRepository memberRepository,
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
    public PrimaryAccount openPrimaryAccount(OpenPrimaryAccountRequest openPrimaryAccountRequest) throws InvalidDepositAmountException, MemberNotFoundException {
        validator.validate(openPrimaryAccountRequest);

        Member member = memberRepository.findById(openPrimaryAccountRequest.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다: " + openPrimaryAccountRequest.getMemberId()));


        String accountNumber = accountNumberGenerator.generate();
        PrimaryAccount newAccount = PrimaryAccount.from(openPrimaryAccountRequest,member,accountNumber);

        return accountRepository.save(newAccount);
    }

    @Override
    @Transactional
    public PrimaryAccount createCoreBankAccount(Member coreMember) {

        String accountNumber = accountNumberGenerator.generate();

        OpenPrimaryAccountRequest coreRequest = new OpenPrimaryAccountRequest(
                coreMember.getMemberId(),
                CORE_INITIAL_BALANCE,
                CORE_ACCOUNT_NICKNAME
        );

        PrimaryAccount coreAccount = PrimaryAccount.from(coreRequest, coreMember, accountNumber);
        return accountRepository.save(coreAccount);
    }


}
