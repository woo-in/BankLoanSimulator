package bankapp.loan.model.common.application;

public enum ApplicationStatus {


    /**
     * 1. 신청완료
     * 고객이 신청서를 막 제출한 상태 (초기 상태)
     */
    APPLIED,

    /**
     * 2. 승인
     * 은행의 심사 결과, 대출이 승인된 상태
     */
    APPROVED,

    /**
     * 3. 거절
     * 은행의 심사 결과, 대출이 거절된 상태
     */
    REJECTED,

    /**
     * 4. 약정완료
     * 승인 이후 고객이 최종적으로 약정서에 서명(계약)한 상태
     */
    CONTRACTED,

    /**
     * 5. 고객취소
     * 고객이 신청을 철회한 상태
     */
    CANCELED

}


