package bankapp.loan.exceptions;

public class InvalidInstallmentException extends RuntimeException {
    public InvalidInstallmentException(String message) {
        super(message);
    }
}
