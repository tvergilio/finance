package uk.ac.leedsbeckett.finance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.ac.leedsbeckett.finance.model.Invoice;
import uk.ac.leedsbeckett.finance.service.InvoiceService;

@Controller
public class PortalController {

    private final InvoiceService invoiceService;

    public PortalController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }


    @GetMapping("/portal")
    public String showPortal(Model model) {
        Invoice invoice = new Invoice();
        model.addAttribute("invoice", new Invoice());
        return "portal";
    }

    @PostMapping("/portal/invoice")
    public String findInvoice(@ModelAttribute Invoice invoice, Model model) {
        Invoice found = invoiceService.getInvoiceByReference(invoice.getReference()).getContent();
        model.addAttribute("invoice", found);
        return "invoice";
    }

    @PostMapping("/portal/pay")
    public String payInvoice(@ModelAttribute Invoice invoice, Model model) {
        Invoice paid = invoiceService.processPayment(invoice.getReference());
        model.addAttribute("invoice", paid);
        return "invoice";
    }
}