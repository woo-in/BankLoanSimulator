package bankapp.loan.underwriting.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 대출 신청 생애주기
 */
@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {

    /**
     * 1. 신청 완료 (Applied)
     */
    APPLIED("심사 대기"),


    /**
     * 은행원(시스템) 심사 결과, 대출이 승인된 상태.
     * (고객에게 대출 실행/약정 버튼이 활성화됨)
     */
    APPROVED("심사 승인"),

    /**
     * DSR 초과, 신용 미달 등의 사유로 심사가 거절된 상태.
     * (더 이상 프로세스 진행 불가)
     */
    REJECTED("심사 거절"),

    /**
     * 고객이 약정서에 서명
     * LoanContract가 생성
     */
    CONTRACTED("계약 완료"),

    /**
     * 7. 취소 (Canceled)
     * 한도 조회 후 고객이 이탈하거나, 신청을 철회한 상태.
     */
    CANCELED("고객 취소");

    private final String description;

    public String getDescription() {
        return description;
    }


}