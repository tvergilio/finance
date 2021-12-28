package uk.ac.leedsbeckett.finance.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.ac.leedsbeckett.finance.controller.AccountController;
import uk.ac.leedsbeckett.finance.exception.AccountNotFoundException;
import uk.ac.leedsbeckett.finance.exception.AccountNotValidException;
import uk.ac.leedsbeckett.finance.model.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountModelAssembler assembler;
    private final InvoiceRepository invoiceRepository;

    public AccountService(AccountRepository accountRepository, AccountModelAssembler assembler, InvoiceRepository invoiceRepository) {
        this.accountRepository = accountRepository;
        this.assembler = assembler;
        this.invoiceRepository = invoiceRepository;
    }

    public EntityModel<Account> getAccountById (Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        return assembler.toModel(populateOutstandingBalance(account));
    }

    public CollectionModel<EntityModel<Account>> getAllAccounts() {
        List<EntityModel<Account>> accounts = accountRepository.findAll()
                .stream()
                .map(this::populateOutstandingBalance)
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(accounts, linkTo(methodOn(AccountController.class).all()).withSelfRel());
    }

    public EntityModel<Account> getAccountByStudentId(String studentId) {
        Account studentAccount = accountRepository.findAccountByStudentId(studentId);
        if (studentAccount == null) {
            throw new AccountNotFoundException(studentId);
        }
        return assembler.toModel(populateOutstandingBalance(studentAccount));
    }

    public ResponseEntity<?> createNewAccount(Account newAccount) {
        if (newAccount.getStudentId() == null || newAccount.getStudentId().isEmpty()) {
            throw new AccountNotValidException();
        }
        Account savedAccount;
        try {
            savedAccount = accountRepository.save(newAccount);
        } catch (DataIntegrityViolationException e) {
            throw new AccountNotValidException("An account already exists for student ID " + newAccount.getStudentId() + ".");
        }
        EntityModel<Account> entityModel = assembler.toModel(savedAccount);
        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    public ResponseEntity<?> updateOrCreateAccount(Account newAccount, Long id) {
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

    public ResponseEntity<?> deleteAccount(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
        accountRepository.delete(account);
        return ResponseEntity.noContent().build();
    }

    private Account populateOutstandingBalance(Account account) {
        if (account != null) {
            List<Invoice> invoices = invoiceRepository.findInvoiceByAccount_IdAndStatus(account.getId(), Status.OUTSTANDING);

            if (invoices != null && !invoices.isEmpty()) {
                account.setHasOutstandingBalance(invoices
                        .stream()
                        .anyMatch(invoice -> invoice.getStatus().equals(Status.OUTSTANDING)));
            }
        }
        return account;
    }
}
