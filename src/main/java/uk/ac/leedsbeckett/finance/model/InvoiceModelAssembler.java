package uk.ac.leedsbeckett.finance.model;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import uk.ac.leedsbeckett.finance.controller.InvoiceController;
import uk.ac.leedsbeckett.finance.exception.InvoiceNotValidException;

@Component
public
class InvoiceModelAssembler implements RepresentationModelAssembler<Invoice, EntityModel<Invoice>> {

    @Override
    public EntityModel<Invoice> toModel(Invoice invoice) {

        if (!isValid(invoice)) {
            throw new InvoiceNotValidException();
        }

        EntityModel<Invoice> invoiceModel = EntityModel.of(invoice,
                linkTo(methodOn(InvoiceController.class).one(invoice.getReference())).withSelfRel(),
                linkTo(methodOn(InvoiceController.class).all()).withRel("invoices"));

        // Conditional links based on status of the Invoice
        if (invoice.getStatus() == Status.OUTSTANDING) {
            invoiceModel.add(linkTo(methodOn(InvoiceController.class).cancel(invoice.getReference())).withRel("cancel"));
            invoiceModel.add(linkTo(methodOn(InvoiceController.class).pay(invoice.getReference())).withRel("pay"));
        }

        return invoiceModel;
    }

    private boolean isValid(Invoice invoice) {
        return invoice.getId() != null
                && invoice.getId() != 0
                && invoice.getReference() != null
                && !invoice.getReference().isEmpty()
                && invoice.getAmount() != null
                && invoice.getDueDate() != null;
    }
}
