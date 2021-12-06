package uk.ac.leedsbeckett.finance.model;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import uk.ac.leedsbeckett.finance.controller.InvoiceController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public
class AccountModelAssembler implements RepresentationModelAssembler<Account, EntityModel<Account>> {

    @Override
    public EntityModel<Account> toModel(Account account) {
        return EntityModel.of(account,
                linkTo(methodOn(InvoiceController.class).one(account.getId())).withSelfRel(),
                linkTo(methodOn(InvoiceController.class).all()).withRel("accounts"));
    }

}
