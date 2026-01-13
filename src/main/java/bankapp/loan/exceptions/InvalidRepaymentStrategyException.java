package bankapp.loan.exceptions;

public class InvalidRepaymentStrategyException extends RuntimeException {
    public InvalidRepaymentStrategyException(String message) {
        super(message);
    }
}
