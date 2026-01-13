package bankapp.loan.exceptions;

public class InvalidRepaymentScheduleException extends RuntimeException {
    public InvalidRepaymentScheduleException(String message) {
        super(message);
    }
}
