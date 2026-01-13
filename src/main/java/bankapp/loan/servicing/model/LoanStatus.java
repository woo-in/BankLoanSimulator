package bankapp.loan.servicing.model;

public enum LoanStatus {
    NORMAL, // 활성
    DELINQUENT, // 단순 연체
    ACCELERATION_NOTICE, // EOD 예정
    ACCELERATION, // EOD 확정
    TERMINATED // 완제,해지
}
