package bankapp.loan.exceptions;

public class OverRepaymentException extends RuntimeException {
    public OverRepaymentException(String message) {
        super(message);
    }
}
