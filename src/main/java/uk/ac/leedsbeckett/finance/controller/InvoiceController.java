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

    @PostMapping("/invoices")
    ResponseEntity<EntityModel<Invoice>> newInvoice(@RequestBody Invoice invoice) {
        return invoiceService.createNewInvoice(invoice);
    }

    @DeleteMapping("/invoices/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        return invoiceService.cancel(id);
    }

    @PutMapping("/invoices/{id}/pay")
    public ResponseEntity<?> pay(@PathVariable Long id) {
        return invoiceService.pay(id);
    }
}