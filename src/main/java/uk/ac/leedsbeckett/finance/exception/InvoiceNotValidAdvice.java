package uk.ac.leedsbeckett.finance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class InvoiceNotValidAdvice {

    /**
     * ResponseBody signals that this advice is rendered straight into the response body.     *
     * ExceptionHandler configures the advice to only respond if an InvoiceNotValidException is thrown.     *
     * ResponseStatus says to issue an HttpStatus.UNPROCESSABLE_ENTITY, i.e. an HTTP 422.
     *
     * The body of the advice generates the content.
     */

    @ResponseBody
    @ExceptionHandler(InvoiceNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    String invoiceNotValidHandler(InvoiceNotValidException ex) {
        return ex.getMessage();
    }
}
