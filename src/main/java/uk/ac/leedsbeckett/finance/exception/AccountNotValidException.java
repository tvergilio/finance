package uk.ac.leedsbeckett.finance.exception;

public class AccountNotValidException extends RuntimeException {

    public AccountNotValidException() {
        super("Not a valid account.");
    }
    public AccountNotValidException(String message) {
        super(message);
    }
}