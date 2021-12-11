package uk.ac.leedsbeckett.finance.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long id) {
        super("Could not find account " + id);
    }
    public AccountNotFoundException(String studentId) {
        super("Could not find account for student ID " + studentId);
    }
}