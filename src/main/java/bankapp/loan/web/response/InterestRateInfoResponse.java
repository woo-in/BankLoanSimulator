package bankapp.loan.web.response;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Data
@Slf4j
public class InterestRateInfoResponse {

    private BigDecimal baseRate;          // (A) 기준 금리
    private BigDecimal productSpread;     // (B) 상품 가산금리
    private BigDecimal creditSpread;      // (C) 신용 가산금리
    private BigDecimal finalInterestRate; // (A+B+C) 최종 금리


    public InterestRateInfoResponse(BigDecimal baseRate, BigDecimal productSpread, BigDecimal creditSpread, BigDecimal finalInterestRate) {
        this.baseRate = baseRate;
        this.productSpread = productSpread;
        this.creditSpread = creditSpread;
        this.finalInterestRate = finalInterestRate;
    }
}
