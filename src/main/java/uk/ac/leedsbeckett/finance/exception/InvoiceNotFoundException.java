package uk.ac.leedsbeckett.finance.exception;

public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(Long id) {
        super("Could not find invoice " + id);
    }

    public InvoiceNotFoundException(String reference) {
        super("Could not find invoice for reference " + reference);
    }

    public InvoiceNotFoundException() {
        super("Could not find invoice.");
    }

}
