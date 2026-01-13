package bankapp.loan.exceptions;

public class InvalidRepaymentStatusException extends RuntimeException {
    public InvalidRepaymentStatusException(String message) {
        super(message);
    }
}
