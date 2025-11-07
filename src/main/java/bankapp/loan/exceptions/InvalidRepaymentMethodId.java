package bankapp.loan.exceptions;

public class InvalidRepaymentMethodId extends RuntimeException {
    public InvalidRepaymentMethodId(String message) {
        super(message);
    }
}
