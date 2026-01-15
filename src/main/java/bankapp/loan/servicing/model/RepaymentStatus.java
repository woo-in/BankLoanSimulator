package bankapp.loan.servicing.model;

public enum RepaymentStatus {
    PLANNED,            // 미래
    PENDING,            // 임박 (5일 전)
    COMPLETE,           // 상환 완료
    MERGED,              //  ACCELERATED로 통합된

    OVERDUE,            // DELINQUENT 시 추가금 쌓임 / ACC_NOTICE 시 그대로
    CRITICAL_OVERDUE,   // ACC_NOTICE 시 추가금 쌓임
    ACCELERATED         // ACC 시 추가금 쌓임 / = CRITICAL + OVERDUE + 나머지 PLANNED

}



