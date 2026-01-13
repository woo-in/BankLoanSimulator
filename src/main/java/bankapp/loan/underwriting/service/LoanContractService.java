package bankapp.loan.underwriting.service;

import bankapp.loan.servicing.model.LoanAccount;
import bankapp.loan.underwriting.model.LoanApplication;
import bankapp.loan.underwriting.model.LoanContract;
import bankapp.loan.origination.web.response.ExistingLoanResponse;
import bankapp.member.model.Member;

import java.util.List;

/**
 * 대출 계약 비즈니스 로직을 정의
 */
public interface LoanContractService {


    /**
     * 대출 계약을 체결
     *
     * @param loanApplication 대출 계약서 작성에 필요한 신청서 정보
     * @param loanAccount 대출 계약서 작성에 필요한 계좌 정보
     * @return 체결 완료된 대출 계약 엔티티
     */
    LoanContract saveLoanContract(LoanApplication loanApplication, LoanAccount loanAccount);


    /**
     * 특정 회원의 대출 계약 목록을 조회 합니다.
     * @return 특정 회원의 대출 계약 목록
     */
    List<LoanContract> findAllByMember(Member member);


    /**
     * 특정 회원의 대출 계약 DTO 목록을 조회 합니다.
     * @return 특정 회원의 대출 계약 DTO 목록
     */
    List<ExistingLoanResponse> findAllContractResponsesByMember(Member member);


}