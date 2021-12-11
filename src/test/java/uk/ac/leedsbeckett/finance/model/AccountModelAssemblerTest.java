package uk.ac.leedsbeckett.finance.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.leedsbeckett.finance.exception.AccountNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class AccountModelAssemblerTest {
    private Account account;

    @Autowired
    private AccountModelAssembler accountModelAssembler;

    @BeforeEach
    public void setUp() {
        String studentId = "c7777777";
        account = new Account(studentId);
        Long id = 1L;
        account.setId(id);
    }

    @Test
    void testToModel_withValidAccount_ReturnsExpectedEntityModel() {
        EntityModel<Account> result = accountModelAssembler.toModel(account);
        assertThat(account.equals(result.getContent()));
        assertThat(result.getLinks().hasSize(2));
        assertThat(result.hasLink("http://localhost/accounts/1"));
        assertThat(result.hasLink("http://localhost/accounts"));
    }

    @Test
    void testToModel_withIdNull_ThrowsException() {
        account.setId(null);
        assertThrows(AccountNotValidException.class, () -> accountModelAssembler.toModel(account),
                "Exception was not thrown.");
    }

    @Test
    void testToModel_withIdZero_ThrowsException() {
        account.setId(0L);
        assertThrows(AccountNotValidException.class, () -> accountModelAssembler.toModel(account),
                "Exception was not thrown.");
    }

    @Test
    void testToModel_withNullArgument_ThrowsException() {
        assertThrows(RuntimeException.class, () -> accountModelAssembler.toModel(null),
                "Exception was not thrown.");
    }
}