package bankapp.loan.exceptions;

public class InvalidInterestRateTypeId extends RuntimeException {
    public InvalidInterestRateTypeId(String message) {
        super(message);
    }
}
