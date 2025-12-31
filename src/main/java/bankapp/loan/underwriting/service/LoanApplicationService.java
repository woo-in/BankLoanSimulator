package bankapp.loan.underwriting.service;

import bankapp.loan.exceptions.InvalidLoanApplication;
import bankapp.loan.exceptions.LoanApplicationNotFoundException;
import bankapp.loan.underwriting.model.LoanApplication;
import bankapp.loan.origination.model.PendingLoanApplication;
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
     * 현재 심사 대기 중인 대출 신청 목록을 조회합니다.
     * <p>
     * 상태가 '신청(APPLIED)'인 건들을 최신순으로 정렬하여 반환하며,
     * 관리자(심사역)가 대출 심사를 수행하기 위한 목록 조회 시 사용됩니다.
     *
     * @return 심사 대기 중인 대출 신청서 리스트
     */
    List<LoanApplication> getAppliedApplications();

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
     * @param reason 거절 이유
     */
    void rejectApplication(Long applicationId , String reason) throws InvalidLoanApplication;

    /**
     * 특정 대출 신청 건을 '승인(APPROVED)' 처리합니다.
     * <p>
     * 해당 신청 건이 존재하지 않거나, 이미 처리된(승인/거절) 상태인 경우 예외가 발생할 수 있습니다.
     * @param applicationId 승인할 대출 신청서의 고유 ID
     */
    void approveApplication(Long applicationId) throws InvalidLoanApplication;

    /**
     * 대출 신청서의 상세 정보를 조회합니다.
     * @param applicationId 조회할 대출 신청서의 고유 ID
     * @return 대출 신청서 엔티티
     */
    Optional<LoanApplication> findById(Long applicationId);

}