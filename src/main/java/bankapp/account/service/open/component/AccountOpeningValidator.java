package bankapp.account.service.open.component;

import bankapp.account.exceptions.AccountMismatchException;
import bankapp.account.exceptions.AccountNotFoundException;
import bankapp.account.exceptions.InvalidDepositAmountException;
import bankapp.account.request.open.OpenLoanAccountRequest;
import bankapp.account.request.open.OpenPrimaryAccountRequest;
import bankapp.member.exceptions.MemberNotFoundException;
import bankapp.member.service.check.MemberCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccountOpeningValidator {
    private final MemberCheckService memberCheckService;

    @Autowired
    public AccountOpeningValidator(MemberCheckService memberCheckService){
        this.memberCheckService = memberCheckService;
    }


    /**
     * 주계좌 개설 요청의 유효성을 검증합니다.
     * <p>
     * 이 메서드는 초기 입금액이 0원 이상인지, 그리고 요청에 포함된 회원 ID가
     * 실제로 존재하는 회원인지를 확인합니다.
     * 모든 검증을 통과하면 정상적으로 반환되고, 그렇지 않으면 적절한 예외를 발생시킵니다.
     *
     * @param request 검증할 계좌 개설 요청 데이터 객체
     * @throws InvalidDepositAmountException 초기 입금액이 0보다 작은 경우
     * @throws MemberNotFoundException       요청에 포함된 회원 ID가 존재하지 않는 경우
     */
    public void validate(OpenPrimaryAccountRequest request) throws InvalidDepositAmountException , MemberNotFoundException {

        if (request.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDepositAmountException("입금액은 0원 이상이어야 합니다.");
        }

        if (!memberCheckService.isMemberIdExist(request.getMemberId())) {
            throw new MemberNotFoundException("memberId " + request.getMemberId() + " not found.");
        }
    }


    /**
     * 대출 계좌 개설 요청의 유효성을 검증합니다.
     * <p>
     * 이 메서드는 초기 입금액이 0원 이상인지,
     * 요청에 포함된 회원 ID가 실제로 존재하는 회원인지,
     * 계약서가 유효한지 확인합니다.
     * 모든 검증을 통과하면 정상적으로 반환되고, 그렇지 않으면 적절한 예외를 발생시킵니다.
     *
     * @param request 검증할 계좌 개설 요청 데이터 객체
     * @throws InvalidDepositAmountException 초기 입금액이 0보다 작은 경우
     * @throws MemberNotFoundException       요청에 포함된 회원 ID가 존재하지 않는 경우
     */
    public void validate(OpenLoanAccountRequest request) throws InvalidDepositAmountException , MemberNotFoundException , AccountNotFoundException ,AccountMismatchException{

        if (request.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDepositAmountException("입금액은 0원 이상이어야 합니다.");
        }

        if (!memberCheckService.isMemberIdExist(request.getMemberId())) {
            throw new MemberNotFoundException("memberId " + request.getMemberId() + " not found.");
        }

        if(request.getRepaymentAccount() == null){
            throw new AccountNotFoundException("대출 계좌에 입출금 계좌를 등록해야 합니다.");
        }

        if(!memberCheckService.findMemberByAccount(request.getRepaymentAccount()).getMemberId().equals(request.getMemberId())){
            throw new AccountMismatchException("계좌와 계정의 명의가 다릅니다.");

        }


    }


}