package bankapp.loan.servicing.model;

public enum RepaymentStatus {

    PLANNED, // 계획 , 먼 미래 (5일보다 먼 미래)
    PENDING, // 대기 (5일 ~ 도달)
    COMPLETE, // 완료 (도달이후 , 상환)
    OVERDUE, // 연체 (도달이후 , 미상환)
    MERGED // 병합됨 - ACCELERATION 상황
}



