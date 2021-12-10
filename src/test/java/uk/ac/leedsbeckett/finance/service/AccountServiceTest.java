package uk.ac.leedsbeckett.finance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.leedsbeckett.finance.model.Account;
import uk.ac.leedsbeckett.finance.model.AccountModelAssembler;
import uk.ac.leedsbeckett.finance.model.AccountRepository;
import uk.ac.leedsbeckett.finance.model.InvoiceRepository;
import static org.assertj.core.api.Assertions.assertThat;

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

    private final String studentId = "c9999999";
    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private InvoiceRepository invoiceRepository;
    private AccountModelAssembler accountModelAssembler;
    @Autowired
    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        Account account = new Account(studentId);
        Mockito.when(accountRepository.findAccountByStudentId(studentId))
                .thenReturn(account);
    }

    @Test
    void testGetAccountByIdReturnsExistingAccount() throws Exception {
        String studentId = "c9999999";
        EntityModel<Account> result = accountService.getAccountByStudentId(studentId);
        assertThat(studentId.equals(result.getContent().getStudentId()));
    }
}