package bankapp.loan.exceptions;

public class InvalidLoanAccountException extends RuntimeException {
    public InvalidLoanAccountException(String message) {
        super(message);
    }
}
