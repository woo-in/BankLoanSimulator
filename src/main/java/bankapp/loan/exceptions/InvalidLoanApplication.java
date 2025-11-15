package bankapp.loan.exceptions;

public class InvalidLoanApplication extends RuntimeException {
    public InvalidLoanApplication(String message) {
        super(message);
    }
}
