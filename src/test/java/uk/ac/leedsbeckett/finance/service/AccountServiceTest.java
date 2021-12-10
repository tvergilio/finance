package uk.ac.leedsbeckett.finance.service;

import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.leedsbeckett.finance.exception.AccountNotFoundException;
import uk.ac.leedsbeckett.finance.model.Account;
import uk.ac.leedsbeckett.finance.model.AccountModelAssembler;
import uk.ac.leedsbeckett.finance.model.AccountRepository;
import uk.ac.leedsbeckett.finance.model.InvoiceRepository;

import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class AccountServiceTest {

    @TestConfiguration
    class EmployeeServiceImplTestContextConfiguration {
        @Bean
        public AccountService accountService() {
            return new AccountService(accountRepository, accountModelAssembler, invoiceRepository);
        }
    }

    private final String studentId = "c7777777";
    private final String anotherStudentId = "c3333333";
    private final Long id = 1L;
    private final Long anotherId = 2L;
    private Account account;
    private Account anotherAccount;
    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private InvoiceRepository invoiceRepository;
    @Autowired
    private AccountModelAssembler accountModelAssembler;
    @Autowired
    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        account = new Account(studentId);
        account.setId(id);
        anotherAccount = new Account(anotherStudentId);
        anotherAccount.setId(anotherId);
        Mockito.when(accountRepository.findAccountByStudentId(studentId))
                .thenReturn(account);
        Mockito.when(accountRepository.findById(id))
                .thenReturn(Optional.of(account));
        Mockito.when(accountRepository.findAll())
                .thenReturn(Arrays.asList(account, anotherAccount));
        Mockito.when(accountRepository.save(account))
                .thenReturn(account);
        Mockito.doNothing().when(accountRepository).deleteById(isA(Long.class));
    }

    @Test
    void testGetAccountByStudentId_withValidID_ReturnsExistingAccount() throws Exception {
        EntityModel<Account> result = accountService.getAccountByStudentId(studentId);
        assertThat(studentId.equals(result.getContent().getStudentId()));
    }

    @Test
    void testGetAccountByStudentId_withInValidID_throwsException() throws Exception {
        assertThrows(ObjectNotFoundException.class, () -> accountService.getAccountByStudentId("dummy"), "Exception was not thrown.");
    }

    @Test
    void testGetAccountByStudentId_withEmptyID_throwsException() throws Exception {
        assertThrows(ObjectNotFoundException.class, () -> accountService.getAccountByStudentId(""), "Exception was not thrown.");
    }

    @Test
    void testGetAccountByStudentId_withNullID_throwsException() throws Exception {
        assertThrows(ObjectNotFoundException.class, () -> accountService.getAccountByStudentId(null), "Exception was not thrown.");
    }

    @Test
    void testGetAccountById_withValidID_ReturnsExistingAccount() throws Exception {
        EntityModel<Account> result = accountService.getAccountById(id);
        assertEquals(id, result.getContent().getId());
    }

    @Test
    void testGetAccountById_withInValidID_throwsException() throws Exception {
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(0L), "Exception was not thrown.");
    }

    @Test
    void testGetAccountById_withNullID_throwsException() throws Exception {
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(null), "Exception was not thrown.");
    }

    @Test
    void testGetAllAccounts_returnsExistingAccounts() throws Exception {
        CollectionModel<EntityModel<Account>> result = accountService.getAllAccounts();
        assertEquals(2, result.getContent().size());
        assertThat(result.getContent().containsAll(Arrays.asList(account, anotherAccount)));
    }

    @Test
    void testCreateNewAccount_withValidData_createsAccount() throws Exception {
        assertEquals(accountModelAssembler.toModel(account), accountService.createNewAccount(account).getBody());
    }

    @Test
    void testDeleteAccount_withValidId_deletesAccount() throws Exception {
        accountService.deleteAccount(id);
        verify(accountRepository, times(1)).deleteById(id);
    }
}