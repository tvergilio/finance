package uk.ac.leedsbeckett.finance.controller;

import org.junit.jupiter.api.*;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setUp() {
        Account account1 = new Account("c6666666");
        account1.setId(1L);
        Account account2 = new Account("c9999999");
        account2.setId(2L);
        accountRepository.saveAll(List.of(account1, account2));
    }

    @Test
    public void a_givenAccount_whenGetAccountById_thenStatus200() throws Exception {
        mvc.perform(get("/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.studentId").value("c6666666"))
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    public void b_givenAccount_whenDelete_thenStatus204() throws Exception {
        mvc.perform(delete("/accounts/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenAccounts_whenGetAccounts_thenStatus200() throws Exception {
        mvc.perform(get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.accountList[0].studentId").value("c6666666"))
                .andExpect(jsonPath("$._embedded.accountList[1].studentId").value("c9999999"));
    }

    @Test
    public void givenNoAccounts_whenGetAccounts_thenStatus200_andLinkToSelf() throws Exception {
        accountRepository.deleteAll();
        mvc.perform(get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._links.self.href").isNotEmpty());
    }


    @Test
    public void givenNoAccount_whenGetAccountById_thenStatus404() throws Exception {
        mvc.perform(get("/accounts/1000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenAccount_whenGetAccountByStudentId_thenStatus200() throws Exception {
        mvc.perform(get("/accounts/student/c6666666")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.studentId").value("c6666666"));
    }

    @Test
    public void givenNoAccount_whenGetAccountByStudentId_thenStatus404() throws Exception {
        mvc.perform(get("/accounts/student/c0000000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenNoAccount_whenPostNewAccount_thenStatus201() throws Exception {
        mvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\": \"c3429928\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value("c3429928"))
                .andExpect(jsonPath("$.hasOutstandingBalance").value(false));
    }

    @Test
    public void givenExistingAccount_whenPostNewAccount_thenStatus422() throws Exception {
        mvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\": \"c6666666\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("An account already exists for student ID c6666666."));
    }

    @Test
    public void whenPostNewAccount_withEmptyAccountValue_thenStatus422() throws Exception {
        mvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\": \"\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void whenPostNewAccount_withEmptyJson_thenStatus400() throws Exception {
        mvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoAccount_whenDelete_thenStatus404() throws Exception {
        mvc.perform(delete("/accounts/1000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Could not find account 1000"));
    }

    @AfterEach
    public void tearDown() {
        accountRepository.deleteAll();
    }
}
