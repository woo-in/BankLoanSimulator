package bankapp.loan.model.common.schedule;

public enum RepaymentStatus {
    PENDING,  // 납입 대기 (배치 프로그램이 처리해야 할 대상)
    PAID,     // 납입 완료 (배치 프로그램이 건너뛸 대상)
    OVERDUE   // 연체 (납입 시도에 실패한 대상. 연체 로직으로 이관)
}
