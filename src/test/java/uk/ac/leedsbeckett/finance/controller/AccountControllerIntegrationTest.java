package uk.ac.leedsbeckett.finance.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.leedsbeckett.finance.model.Account;
import uk.ac.leedsbeckett.finance.model.AccountRepository;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")

public class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void givenAccounts_whenGetAccounts_thenStatus200() throws Exception {
        Account account1 = new Account("c6666666");
        account1.setId(1L);
        Account account2 = new Account("c9999999");
        account2.setId(2L);
        accountRepository.saveAll(List.of(account1, account2));

        mvc.perform(get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.accountList[0].studentId").value("c6666666"));
    }
}
