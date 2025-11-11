package bankapp.loan.exceptions;

public class InvalidLoanProduct extends RuntimeException {
    public InvalidLoanProduct(String message) {
        super(message);
    }
}
