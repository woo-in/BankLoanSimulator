package bankapp.loan.origination.web.response;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Data
@Slf4j
public class InterestRateInfoResponse {

    private BigDecimal baseRate;
    private BigDecimal productSpread;
    private BigDecimal creditSpread;

    private BigDecimal minSelectionSpread;
    private BigDecimal maxSelectionSpread;

    // todo : 임시로 유지
    private BigDecimal FinalInterestRate;


    private BigDecimal minFinalInterestRate;
    private BigDecimal maxFinalInterestRate;

    public InterestRateInfoResponse(BigDecimal baseRate,
                                    BigDecimal productSpread,
                                    BigDecimal creditSpread,
                                    BigDecimal minSelectionSpread,
                                    BigDecimal maxSelectionSpread) {
        this.baseRate = baseRate;
        this.productSpread = productSpread;
        this.creditSpread = creditSpread;
        this.minSelectionSpread = minSelectionSpread;
        this.maxSelectionSpread = maxSelectionSpread;

        BigDecimal baseTotal = baseRate.add(productSpread).add(creditSpread);
        this.FinalInterestRate = baseTotal;
        this.minFinalInterestRate = baseTotal.add(minSelectionSpread);
        this.maxFinalInterestRate = baseTotal.add(maxSelectionSpread);
    }
}
