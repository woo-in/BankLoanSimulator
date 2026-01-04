package bankapp.loan.underwriting.service;

import bankapp.account.request.open.OpenLoanAccountRequest;
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
     * 대출 계약을 체결하고 저장합니다.
     *
     * @param openLoanAccountRequest 대출 계좌 개설에 필요한 요청 정보
     * @param loanApplication        최종 승인된 대출 신청서 엔티티 (계약의 근거 데이터)
     * @return 체결 완료된 대출 계약 엔티티
     */
    LoanContract saveLoanContract(OpenLoanAccountRequest openLoanAccountRequest,
                                  LoanApplication loanApplication);


    LoanContract saveLoanContract(LoanApplication loanApplication);


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