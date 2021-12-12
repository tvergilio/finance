package uk.ac.leedsbeckett.finance.model;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import uk.ac.leedsbeckett.finance.controller.AccountController;
import uk.ac.leedsbeckett.finance.exception.AccountNotValidException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public
class AccountModelAssembler implements RepresentationModelAssembler<Account, EntityModel<Account>> {

    @Override
    public EntityModel<Account> toModel(Account account) {
        if (account.getId() == null || account.getId() == 0) {
            throw new AccountNotValidException();
        }
        return EntityModel.of(account,
                linkTo(methodOn(AccountController.class).getStudentAccount(account.getStudentId())).withSelfRel(),
                linkTo(methodOn(AccountController.class).all()).withRel("accounts"));
    }

}
