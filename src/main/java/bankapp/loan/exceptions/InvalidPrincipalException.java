package bankapp.loan.exceptions;

public class InvalidPrincipalException extends RuntimeException {
    public InvalidPrincipalException(String message) {
        super(message);
    }
}
