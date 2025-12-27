package bankapp.loan.exceptions;

public class InvalidPendingLoan extends RuntimeException {
    public InvalidPendingLoan(String message) {
        super(message);
    }
}
