package uk.ac.leedsbeckett.finance.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.leedsbeckett.finance.model.Account;
import uk.ac.leedsbeckett.finance.model.AccountRepository;

import java.util.List;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
@AutoConfigureRestDocs(outputDir = "build/snippets")
public class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AccountRepository accountRepository;

    private List<FieldDescriptor> accountResponseFieldDescriptor;
    private List<LinkDescriptor> linkDescriptors;

    @BeforeEach
    public void setUp() {
        Account account1 = new Account("c6666666");
        account1.setId(1L);
        Account account2 = new Account("c9999999");
        account2.setId(2L);
        accountRepository.saveAll(List.of(account1, account2));
        configureRestDocumentation();
    }

    @Test
    public void a_givenAccount_whenGetAccountById_thenStatus200() throws Exception {
        mvc.perform(get("/accounts/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.studentId").value("c6666666"))
                .andExpect(jsonPath("$.id").value("1"))

                .andDo(document("get-account-by-id",
                        pathParameters(parameterWithName("id").description("The id of the account to return.")),
                        links(linkDescriptors),
                        responseFields(accountResponseFieldDescriptor)));
    }

    @Test
    public void b_givenAccount_whenDelete_thenStatus204() throws Exception {
        mvc.perform(delete("/accounts/{id}", 3)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(document("accounts", pathParameters(
                        parameterWithName("id").description("The id of the account to delete"))))

                .andDo(document("delete-account-by-id",
                        pathParameters(parameterWithName("id").description("The id of the account to delete."))));
    }

    @Test
    public void givenAccounts_whenGetAccounts_thenStatus200() throws Exception {
        mvc.perform(get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.accountList[0].studentId").value("c6666666"))
                .andExpect(jsonPath("$._embedded.accountList[1].studentId").value("c9999999"))

                .andDo(document("get-accounts",
                        links(halLinks(), linkWithRel("self").description("Link to this resource.")),
                        responseFields(fieldWithPath("_embedded.accountList[]").description("A list of accounts"),
                                subsectionWithPath("_links").ignored())
                                .andWithPrefix("_embedded.accountList[].", accountResponseFieldDescriptor)));
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
                .andExpect(jsonPath("$.hasOutstandingBalance").value(false))

                .andDo(document("create-account",
                requestFields(fieldWithPath("studentId").description("The external student ID.")),
                links(linkDescriptors),
                responseFields(accountResponseFieldDescriptor)));
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

    private void configureRestDocumentation() {
        accountResponseFieldDescriptor = List.of(
                fieldWithPath("id").description("The internal ID of the account."),
                fieldWithPath("studentId").description("The external student ID."),
                fieldWithPath("hasOutstandingBalance").description("Whether the account has an outstanding balance."),
                subsectionWithPath("_links").ignored()
        );
        linkDescriptors = List.of(linkWithRel("self").description("Link to this resource."),
                linkWithRel("accounts").description("Link to all accounts."));
    }

    @AfterEach
    public void tearDown() {
        accountRepository.deleteAll();
    }
}
