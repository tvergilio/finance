package uk.ac.leedsbeckett.finance.controller;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.leedsbeckett.finance.exception.AccountNotFoundException;
import uk.ac.leedsbeckett.finance.model.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public
class AccountController {

    private final AccountRepository accountRepository;
    private final InvoiceRepository invoiceRepository;
    private final AccountModelAssembler assembler;

    AccountController(AccountRepository repository, InvoiceRepository invoiceRepository, AccountModelAssembler assembler) {
        this.accountRepository = repository;
        this.invoiceRepository = invoiceRepository;
        this.assembler = assembler;
    }

    @GetMapping("/accounts")
    public CollectionModel<EntityModel<Account>> all() {
        List<EntityModel<Account>> accounts = accountRepository.findAll()
                .stream()
                .map(this::populateOutstandingBalance)
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(accounts, linkTo(methodOn(AccountController.class).all()).withSelfRel());
    }

    @PostMapping("/accounts")
    ResponseEntity<?> newAccount(@RequestBody Account newAccount) {
        EntityModel<Account> entityModel = assembler.toModel(accountRepository.save(newAccount));
        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }


    @GetMapping("/accounts/student/{studentId}")
    public EntityModel<Account> getStudentAccount(@PathVariable String studentId) {
        Account studentAccount = accountRepository.findAccountByStudentId(studentId);
        return assembler.toModel(populateOutstandingBalance(studentAccount));
    }

    @GetMapping("/accounts/{id}")
    public EntityModel<Account> one(@PathVariable Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        return assembler.toModel(populateOutstandingBalance(account));
    }

    @PutMapping("/accounts/{id}")
    ResponseEntity<?> editAccount(@RequestBody Account newAccount, @PathVariable Long id) {

        Account updatedAccount = accountRepository.findById(id)
                .map(account -> {
                    account.setStudentId(newAccount.getStudentId());
                    return accountRepository.save(account);
                })
                .orElseGet(() -> {
                    newAccount.setId(id);
                    return accountRepository.save(newAccount);
                });
        EntityModel<Account> entityModel = assembler.toModel(updatedAccount);
        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @DeleteMapping("/accounts/{id}")
    ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        accountRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    private Account populateOutstandingBalance(Account account) {
        List<Invoice> invoices = invoiceRepository.findInvoiceByAccount_IdAndStatus(account.getId(), Status.OUTSTANDING);

        if (invoices != null && !invoices.isEmpty()) {
            account.setHasOutstandingBalance(invoices
                    .stream()
                    .anyMatch(invoice -> invoice.getStatus().equals(Status.OUTSTANDING)));
        }
        return account;
    }

}