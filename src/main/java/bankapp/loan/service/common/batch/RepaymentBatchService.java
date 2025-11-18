package bankapp.loan.service.common.batch;

import java.time.LocalDate;

public interface RepaymentBatchService {
    void processDailyRepayments() ;
    void processRepayments(LocalDate localDate);
}
