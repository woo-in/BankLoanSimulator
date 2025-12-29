package bankapp.loan.origination.model;

public enum PendingLoanApplicationStatus {

    /**
     * 1단계: 개인 정보 입력 완료
     */
    FINANCIAL_INFO_SUBMITTED,

    /**
     * 2단계: 신청 정보 입력 완료
     */
    TERMS_SELECTED,

    /**
     * 3단계: 최종 신청 완료
     */
    COMPLETED

}
