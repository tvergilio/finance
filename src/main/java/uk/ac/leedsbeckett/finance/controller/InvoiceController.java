package uk.ac.leedsbeckett.finance.controller;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.leedsbeckett.finance.exception.InvoiceNotFoundException;
import uk.ac.leedsbeckett.finance.model.Invoice;
import uk.ac.leedsbeckett.finance.model.InvoiceModelAssembler;
import uk.ac.leedsbeckett.finance.model.InvoiceRepository;
import uk.ac.leedsbeckett.finance.model.Status;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public
class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceModelAssembler assembler;

    InvoiceController(InvoiceRepository invoiceRepository, InvoiceModelAssembler assembler) {

        this.invoiceRepository = invoiceRepository;
        this.assembler = assembler;
    }

    @GetMapping("/invoices")
    public CollectionModel<EntityModel<Invoice>> all() {

        List<EntityModel<Invoice>> invoices = invoiceRepository.findAll().stream() //
                .map(assembler::toModel) //
                .collect(Collectors.toList());

        return CollectionModel.of(invoices, //
                linkTo(methodOn(InvoiceController.class).all()).withSelfRel());
    }

    @GetMapping("/invoices/{id}")
    public EntityModel<Invoice> one(@PathVariable Long id) {

        Invoice invoice = invoiceRepository.findById(id) //
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        return assembler.toModel(invoice);
    }

    @PostMapping("/invoices")
    ResponseEntity<EntityModel<Invoice>> newInvoice(@RequestBody Invoice invoice) {

        invoice.setStatus(Status.OUTSTANDING);
        Invoice newInvoice = invoiceRepository.save(invoice);

        return ResponseEntity //
                .created(linkTo(methodOn(InvoiceController.class).one(newInvoice.getId())).toUri()) //
                .body(assembler.toModel(newInvoice));
    }

    @DeleteMapping("/invoices/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

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

    @PutMapping("/invoices/{id}/pay")
    public ResponseEntity<?> pay(@PathVariable Long id) {

        Invoice invoice = invoiceRepository.findById(id) //
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        if (invoice.getStatus() == Status.OUTSTANDING) {
            invoice.setStatus(Status.PAID);
            return ResponseEntity.ok(assembler.toModel(invoiceRepository.save(invoice)));
        }

        return ResponseEntity //
                .status(HttpStatus.METHOD_NOT_ALLOWED) //
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
                .body(Problem.create() //
                        .withTitle("Method not allowed") //
                        .withDetail("You can't pay an invoice that is in the " + invoice.getStatus() + " status"));
    }
}