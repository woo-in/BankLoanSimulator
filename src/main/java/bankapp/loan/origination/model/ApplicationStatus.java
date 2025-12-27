package bankapp.loan.origination.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 대출 신청의 전체 생애주기(Lifecycle)를 관리하는 상태값 정의
 * (PendingLoanApplication 통합 버전)
 */
@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {


    /**
     * 유저 :  총 자산 규모 / 연 소득 / 고정 지출 / 타행 대출 현황 입력
     */
    DRAFT("작성 중"),

    /**
    * 유저 : 대출금 / 대출 기간 / 상환 방법 / 금리 종류 입력
     */
    PRE_CHECKED("한도 산출 완료"),


    // --- [2. 대출 신청 단계 (Submission)] ---

    /**
     * 3. 신청 완료 (Applied)
     * 고객이 산출된 한도를 확인하고, 최종적으로 심사를 요청한 상태.
     * (은행원의 심사 대기 목록에 노출됨)
     */
    APPLIED("심사 대기"),


    // --- [3. 심사 단계 (Underwriting)] ---

    /**
     * 4. 승인 (Approved)
     * 은행원(시스템) 심사 결과, 대출이 승인된 상태.
     * (고객에게 대출 실행/약정 버튼이 활성화됨)
     */
    APPROVED("심사 승인"),

    /**
     * 5. 거절 (Rejected)
     * DSR 초과, 신용 미달 등의 사유로 심사가 거절된 상태.
     * (더 이상 프로세스 진행 불가)
     */
    REJECTED("심사 거절"),


    // --- [4. 실행 단계 (Closing)] ---

    /**
     * 6. 계약 완료 (Contracted)
     * 고객이 약정서에 서명하고 대출 실행(입금)까지 완료된 최종 상태.
     * (이 시점에 LoanContract가 생성됨)
     */
    CONTRACTED("계약 완료"),


    // --- [5. 예외 종료] ---

    /**
     * 7. 취소 (Canceled)
     * 한도 조회 후 고객이 이탈하거나, 신청을 철회한 상태.
     */
    CANCELED("고객 취소");

    private final String description;
}