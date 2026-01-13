package bankapp.loan.exceptions;

public class ActiveLoanContractNotFoundException extends RuntimeException {
    public ActiveLoanContractNotFoundException(String message) {
        super(message);
    }
}
