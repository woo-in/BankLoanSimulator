package bankapp.loan.exceptions;

public class InvalidLoanType extends RuntimeException {
    public InvalidLoanType(String message) {
        super(message);
    }
}
