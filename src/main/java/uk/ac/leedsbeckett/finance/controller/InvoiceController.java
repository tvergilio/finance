package uk.ac.leedsbeckett.finance.controller;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.leedsbeckett.finance.model.Invoice;
import uk.ac.leedsbeckett.finance.service.InvoiceService;

@RestController
public
class InvoiceController {

    private final InvoiceService invoiceService;

    InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/invoices")
    public CollectionModel<EntityModel<Invoice>> all() {
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/invoices/{id}")
    public EntityModel<Invoice> one(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id);
    }

    @GetMapping("/invoices/reference/{reference}")
    public EntityModel<Invoice> one(@PathVariable String reference) {
        return invoiceService.getInvoiceByReference(reference);
    }

    @PostMapping("/invoices")
    ResponseEntity<?> newInvoice(@RequestBody Invoice invoice) {
        return invoiceService.createNewInvoice(invoice);
    }

    @DeleteMapping("/invoices/{reference}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String reference) {
        return invoiceService.cancel(reference);
    }

    @PutMapping("/invoices/{reference}/pay")
    public ResponseEntity<?> pay(@PathVariable String reference) {
        return invoiceService.pay(reference);
    }
}