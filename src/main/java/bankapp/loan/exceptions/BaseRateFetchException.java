package bankapp.loan.exceptions;

public class BaseRateFetchException extends RuntimeException {
    public BaseRateFetchException(String message) {
        super(message);
    }
}
