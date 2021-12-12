package uk.ac.leedsbeckett.finance.service;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.ac.leedsbeckett.finance.controller.InvoiceController;
import uk.ac.leedsbeckett.finance.exception.InvoiceNotFoundException;
import uk.ac.leedsbeckett.finance.exception.InvoiceNotValidException;
import uk.ac.leedsbeckett.finance.model.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InvoiceService {

    private final AccountRepository accountRepository;
    private final InvoiceModelAssembler assembler;
    private final InvoiceRepository invoiceRepository;

    public InvoiceService(AccountRepository accountRepository, InvoiceModelAssembler assembler, InvoiceRepository invoiceRepository) {
        this.accountRepository = accountRepository;
        this.assembler = assembler;
        this.invoiceRepository = invoiceRepository;
    }

    public EntityModel<Invoice> getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        return assembler.toModel(invoice);
    }

    public CollectionModel<EntityModel<Invoice>> getAllInvoices() {
        List<EntityModel<Invoice>> invoices = invoiceRepository.findAll()
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(invoices, linkTo(methodOn(InvoiceController.class).all()).withSelfRel());
    }

    public ResponseEntity<?> createNewInvoice(Invoice invoice) {
        if (!isInvoiceProcessable(invoice)) {
            throw new InvoiceNotValidException("You can't create an invoice without a valid student ID.");
        }
        invoice.setStatus(Status.OUTSTANDING);
        invoice.setAccount(accountRepository.findAccountByStudentId(invoice.getStudentId()));
        invoice.populateReference();
        Invoice newInvoice = invoiceRepository.save(invoice);

        return ResponseEntity
                .created(linkTo(methodOn(InvoiceController.class).one(newInvoice.getId())).toUri())
                .body(assembler.toModel(newInvoice));
    }

    public ResponseEntity<?> cancel(String reference) {
        Invoice invoice = invoiceRepository.findInvoiceByReference(reference);

        if (invoice == null) {
            throw new InvoiceNotFoundException(reference);
        }

        if (invoice.getStatus() == Status.OUTSTANDING) {
            invoice.setStatus(Status.CANCELLED);
            return ResponseEntity.ok(assembler.toModel(invoiceRepository.save(invoice)));
        }

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .body(Problem.create()
                        .withTitle("Method not allowed")
                        .withDetail("You can't cancel an invoice that is in the " + invoice.getStatus() + " status"));
    }

    public ResponseEntity<?> pay(String reference) {
        Invoice invoice;
        try {
            invoice = processPayment(reference);
        } catch (UnsupportedOperationException exception) {
            return ResponseEntity
                    .status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                    .body(Problem.create()
                            .withTitle("Method not allowed")
                            .withDetail(exception.getMessage()));
        }
            return ResponseEntity.ok(assembler.toModel(invoiceRepository.save(invoice)));
    }

    public EntityModel<Invoice> getInvoiceByReference(String reference) {
        Invoice invoice = invoiceRepository.findInvoiceByReference(reference);
        if (invoice == null) {
            throw new InvoiceNotFoundException(reference);
        }
        return assembler.toModel(invoice);
    }

    private boolean isInvoiceProcessable(Invoice invoice) {
        return invoice != null &&
                invoice.getAccount() != null &&
                invoice.getStudentId() != null &&
                !invoice.getStudentId().isEmpty() &&
                accountRepository.findAccountByStudentId(invoice.getStudentId()) != null;
    }

    public Invoice processPayment(String reference) throws UnsupportedOperationException {
        Invoice invoice = invoiceRepository.findInvoiceByReference(reference);

        if (invoice == null) {
            throw new InvoiceNotFoundException(reference);
        }

        if (invoice.getStatus() == Status.OUTSTANDING) {
            invoice.setStatus(Status.PAID);
            return invoiceRepository.save(invoice);
        } else {
            throw new UnsupportedOperationException("You can't pay an invoice that is in the " + invoice.getStatus() + " status");
        }
    }

}
