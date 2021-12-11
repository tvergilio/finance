package uk.ac.leedsbeckett.finance.exception;

public class UserNotValidException extends RuntimeException {

    public UserNotValidException() {
        super("Not a valid user.");
    }
}