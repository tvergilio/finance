package uk.ac.leedsbeckett.finance.controller;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.leedsbeckett.finance.model.Account;
import uk.ac.leedsbeckett.finance.service.AccountService;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController
public
class AccountController {

    private final AccountService accountService;

    AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/accounts")
    public CollectionModel<EntityModel<Account>> all() {
        return accountService.getAllAccounts();
    }

    @PostMapping("/accounts")
    ResponseEntity<?> newAccount(@RequestBody @NotNull @NotEmpty Account newAccount) {
        return accountService.createNewAccount(newAccount);
    }

    @GetMapping("/accounts/student/{studentId}")
    public EntityModel<Account> getStudentAccount(@PathVariable String studentId) {
        return accountService.getAccountByStudentId(studentId);
    }

    @GetMapping("/accounts/{id}")
    public EntityModel<Account> one(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    @PutMapping("/accounts/{id}")
    ResponseEntity<?> editAccount(@RequestBody Account newAccount, @PathVariable Long id) {
        return accountService.updateOrCreateAccount(newAccount, id);
    }

    @DeleteMapping("/accounts/{id}")
    ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        return accountService.deleteAccount(id);
    }

}