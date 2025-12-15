package bankapp.loan.servicing.service;

import java.time.LocalDate;

public interface RepaymentBatchService {
    void processDailyRepayments() ;
    void processRepayments(LocalDate localDate);
}
