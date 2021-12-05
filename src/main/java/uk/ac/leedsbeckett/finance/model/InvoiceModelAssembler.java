package uk.ac.leedsbeckett.finance.model;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import uk.ac.leedsbeckett.finance.controller.InvoiceController;

@Component
public
class InvoiceModelAssembler implements RepresentationModelAssembler<Invoice, EntityModel<Invoice>> {

    @Override
    public EntityModel<Invoice> toModel(Invoice invoice) {

        // Unconditional links to single-item resource and aggregate root

        EntityModel<Invoice> invoiceModel = EntityModel.of(invoice,
                linkTo(methodOn(InvoiceController.class).one(invoice.getId())).withSelfRel(),
                linkTo(methodOn(InvoiceController.class).all()).withRel("invoices"));

        // Conditional links based on state of the Invoice

        if (invoice.getStatus() == Status.OUTSTANDING) {
            invoiceModel.add(linkTo(methodOn(InvoiceController.class).cancel(invoice.getId())).withRel("cancel"));
            invoiceModel.add(linkTo(methodOn(InvoiceController.class).pay(invoice.getId())).withRel("pay"));
        }

        return invoiceModel;
    }
}
