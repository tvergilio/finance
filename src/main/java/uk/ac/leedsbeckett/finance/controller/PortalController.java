package uk.ac.leedsbeckett.finance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.ac.leedsbeckett.finance.model.Invoice;
import uk.ac.leedsbeckett.finance.service.InvoiceService;

import javax.validation.Valid;

@Controller
public class PortalController {

    private final InvoiceService invoiceService;

    public PortalController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping({ "/"})
    public String redirectToHome(Model model) {
        return "redirect:/portal";
    }

    @GetMapping({ "/portal", "/portal/invoice"})
    public String showPortal(Model model) {
        return invoiceService.showPortal(model);
    }

    @PostMapping("/portal/invoice")
    public String findInvoice(@ModelAttribute @Valid Invoice invoice, BindingResult bindingResult, Model model) {
        return invoiceService.findInvoiceThroughPortal(invoice, bindingResult, model);
    }

    @PostMapping("/portal/pay")
    public String payInvoice(@ModelAttribute Invoice invoice, Model model) {
        return invoiceService.payInvoiceThroughPortal(invoice, model);
    }
}