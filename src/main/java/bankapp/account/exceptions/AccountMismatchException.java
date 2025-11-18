package bankapp.account.exceptions;

public class AccountMismatchException extends RuntimeException {
  public AccountMismatchException(String message) {
    super(message);
  }
}
