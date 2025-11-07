package bankapp.loan.exceptions;

public class InvalidFinancialGradeException extends RuntimeException {
    public InvalidFinancialGradeException(String message) {
        super(message);
    }
}
