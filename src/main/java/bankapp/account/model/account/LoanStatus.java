package bankapp.account.model.account;

public enum LoanStatus {
    NORMAL, // 활성
    DELINQUENT, // 1차 체납
    EOD_DEFAULT, // EOD
    FULLY_PAID, // 다 갚음
    INACTIVE // 비활성
}
