package bankapp.loan.underwriting.service;

import bankapp.loan.exceptions.InvalidLoanApplication;
import bankapp.loan.exceptions.LoanApplicationNotFoundException;
import bankapp.loan.underwriting.model.LoanApplication;
import bankapp.loan.origination.model.PendingLoanApplication;
import bankapp.loan.underwriting.web.customerdto.ContractAuthRequest;
import bankapp.loan.underwriting.web.customerdto.ExecutionInfoRequest;
import bankapp.loan.underwriting.web.request.ApprovedLoanApplicationDto;
import bankapp.loan.underwriting.web.request.RejectedLoanApplicationDto;
import bankapp.member.exceptions.IncorrectPasswordException;
import bankapp.member.exceptions.PasswordMismatchException;
import bankapp.member.model.Member;

import java.util.List;
import java.util.Optional;

/**
 * 대출 신청서에 대한 비지니스 로직을 명시
 */
public interface LoanApplicationService {



    /**
     * 고객의 대출 신청 정보를 접수하여 저장합니다.
     * 최종 대출 신청서(LoanApplication) 엔티티를 생성하고 '신청(APPLIED)' 상태로 저장합니다.
     * @param pendingLoanApplication   대출 신청 상태
     */
    void saveLoanApplication(PendingLoanApplication pendingLoanApplication);



    /**
     * 현재 심사 대기 중인 대출 신청 목록 전체를 조회합니다.
     * <p>
     * 상태가 (APPLIED) 인 건들을 최신순으로 정렬하여 반환하며,
     * 관리자(심사역)가 대출 심사를 수행하기 위한 목록 조회 시 사용됩니다.
     *
     * @return 대출 신청서 리스트
     */
    List<LoanApplication> getAppliedApplications();


    /**
     * 특정 회원(Member)이 신청한 모든 대출 신청 내역을 조회합니다.
     * <p>
     * 고객의 '나의 대출 신청 내역' 조회 화면에서 사용되며,
     * 해당 회원이 신청한 모든 건을 최신순으로 정렬하여 반환해야 합니다.
     *
     * @param memberId 조회할 회원의 고유 ID
     * @return 해당 회원의 대출 신청서 리스트
     */
    List<LoanApplication> getLoanApplicationsByMemberId(Long memberId);


    /**
     * 대출 신청 반환
     * @param applicationId 대출 신청서의 고유 ID
     * @return 심사 대기 중인 대출 신청서 리스트
     * @throws LoanApplicationNotFoundException 대출신청서 없음
     */
    LoanApplication getLoanApplicationById(long applicationId) throws LoanApplicationNotFoundException;


    /**
     * 특정 대출 신청 건을 '거절(REJECTED)' 처리합니다.
     * <p>
     * 해당 신청 건이 존재하지 않거나, 이미 처리된(승인/거절) 상태인 경우 예외가 발생할 수 있습니다.
     * @param applicationId 거절할 대출 신청서의 고유 ID
     * @param rejectedLoanApplicationDto 거절 DTO
     */
    void rejectApplication(Long applicationId , RejectedLoanApplicationDto rejectedLoanApplicationDto) throws InvalidLoanApplication;

    /**
     * 특정 대출 신청 건을 '승인(APPROVED)' 처리합니다.
     * <p>
     * 해당 신청 건이 존재하지 않거나, 이미 처리된(승인/거절) 상태인 경우 예외가 발생할 수 있습니다.
     * @param applicationId 승인할 대출 신청서의 고유 ID
     * @param approvedLoanApplicationDto 승인 DTO
     */
    void approveApplication(Long applicationId , ApprovedLoanApplicationDto approvedLoanApplicationDto) throws InvalidLoanApplication;

    /**
     * 특정 대출 신청 건을 '취소(CANCEL)' 처리합니다.
     * <p>
     * 해당 신청 건이 존재하지 않거나, 이미 처리된 상태인 경우 예외가 발생할 수 있습니다.
     * @param applicationId 승인할 대출 신청서의 고유 ID
     */
    void cancelApplication(Long applicationId) throws InvalidLoanApplication;

    /**
     * 대출 실행 정보를 신청서에 업데이트 합니다.
     * <p>
     * 해당 신청 건이 존재하지 않거나, 이미 처리된 상태인 경우 예외가 발생할 수 있습니다.
     * @param applicationId 승인할 대출 신청서의 고유 ID
     */
    void updateApplicationExecutionInfo(Long applicationId , ExecutionInfoRequest executionInfoRequest) throws InvalidLoanApplication;


    // todo : 일단은 대출 계약과 관련된 모든 걸 다 때려 박아 두었습니다.
    /**
     * 일단은 대출 계약과 관련된 모든 걸 다 때려 박아 두었습니다.
     * 비밀번호 확인 -> 대출계좌 생성 -> 대출 계약서 작성 -> 입금 진행 -> 스케줄러 작성 -> 신청서 상태 변경
     * <p>
     * @param applicationId 승인할 대출 신청서의 고유 ID
     * @param loginMember 비밀번호 확인할 로그인 정보
     * @param contractAuthRequest 대출 계약전 비밀번호 체크 입력
     */
    void signContract(Long applicationId , Member loginMember , ContractAuthRequest contractAuthRequest) throws InvalidLoanApplication , IncorrectPasswordException;


    /**
     * 대출 신청서의 상세 정보를 조회합니다.
     * @param applicationId 조회할 대출 신청서의 고유 ID
     * @return 대출 신청서 엔티티
     */
    Optional<LoanApplication> findById(Long applicationId);

}