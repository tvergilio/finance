package uk.ac.leedsbeckett.finance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.leedsbeckett.finance.controller.AccountController;
import uk.ac.leedsbeckett.finance.exception.AccountNotFoundException;
import uk.ac.leedsbeckett.finance.model.Account;
import uk.ac.leedsbeckett.finance.model.AccountModelAssembler;
import uk.ac.leedsbeckett.finance.model.AccountRepository;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class AccountServiceTest {

    private final String studentId = "c7777777";
    private final Long id = 1L;
    private Account account;
    private Account anotherAccount;
    @MockBean
    private AccountRepository accountRepository;
    @SpyBean
    private AccountModelAssembler accountModelAssembler;
    @Autowired
    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        account = new Account(studentId);
        account.setId(id);
        String anotherStudentId = "c3333333";
        anotherAccount = new Account(anotherStudentId);
        Long anotherId = 2L;
        anotherAccount.setId(anotherId);
        Mockito.when(accountRepository.findAccountByStudentId(studentId))
                .thenReturn(account);
        Mockito.when(accountRepository.findById(id))
                .thenReturn(Optional.of(account));
        Mockito.when(accountRepository.findAll())
                .thenReturn(Arrays.asList(account, anotherAccount));
        Mockito.when(accountRepository.save(account))
                .thenReturn(account);
        Mockito.doNothing().when(accountRepository).delete(account);
    }

    @Test
    void testGetAccountByStudentId_withValidID_ReturnsExistingAccount() {
        EntityModel<Account> result = accountService.getAccountByStudentId(studentId);
        assertThat(studentId.equals(result.getContent().getStudentId()));
        verify(accountModelAssembler, times(1)).toModel(account);
    }

    @Test
    void testGetAccountByStudentId_withInValidID_throwsException() {
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByStudentId("dummy"),
                "Exception was not thrown.");
        verify(accountModelAssembler, times(0)).toModel(any());
    }

    @Test
    void testGetAccountByStudentId_withEmptyID_throwsException() {
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByStudentId(""),
                "Exception was not thrown.");
        verify(accountModelAssembler, times(0)).toModel(any());
    }

    @Test
    void testGetAccountByStudentId_withNullID_throwsException() {
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByStudentId(null),
                "Exception was not thrown.");
        verify(accountModelAssembler, times(0)).toModel(any());
    }

    @Test
    void testGetAccountById_withValidID_ReturnsExistingAccount() {
        EntityModel<Account> result = accountService.getAccountById(id);
        assertEquals(id, result.getContent().getId());
        verify(accountModelAssembler, times(1)).toModel(account);
    }

    @Test
    void testGetAccountById_withInValidID_throwsException() {
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(0L),
                "Exception was not thrown.");
        verify(accountModelAssembler, times(0)).toModel(any());
    }

    @Test
    void testGetAccountById_withNullID_throwsException() {
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(null),
                "Exception was not thrown.");
        verify(accountModelAssembler, times(0)).toModel(any());
    }

    @Test
    void testGetAllAccounts_returnsExistingAccounts() {
        CollectionModel<EntityModel<Account>> result = accountService.getAllAccounts();
        assertEquals(2, result.getContent().size());
        assertThat(result.getContent().containsAll(Arrays.asList(account, anotherAccount)));
        verify(accountModelAssembler, times(1)).toModel(account);
        verify(accountModelAssembler, times(1)).toModel(anotherAccount);
    }

    @Test
    void testCreateNewAccount_withValidData_createsAccount() {
        EntityModel<Account> accountEntityModel = EntityModel.of(account,
                linkTo(methodOn(AccountController.class).getStudentAccount(account.getStudentId())).withSelfRel(),
                linkTo(methodOn(AccountController.class).all()).withRel("accounts"));
        assertEquals(accountEntityModel, accountService.createNewAccount(account).getBody());
        verify(accountModelAssembler, times(1)).toModel(account);
    }

    @Test
    void testDeleteAccount_withValidId_deletesAccount() {
        accountService.deleteAccount(id);
        verify(accountRepository, times(1)).delete(account);
        verify(accountModelAssembler, times(0)).toModel(any());
    }
}