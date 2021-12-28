package uk.ac.leedsbeckett.finance.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.leedsbeckett.finance.model.*;

import java.time.LocalDate;
import java.time.Month;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ActiveProfiles("test")
public class PortalControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private String invoiceReference;
    private Invoice outstandingInvoice;

    @BeforeEach
    public void setUp() {
        Account account = new Account("c6666666");
        account.setId(1L);
        accountRepository.save(account);
        outstandingInvoice = new Invoice(10.00, LocalDate.of(2022, Month.FEBRUARY, 15), Type.LIBRARY_FINE, account);
        outstandingInvoice.setStatus(Status.OUTSTANDING);
        Invoice outstandingInvoiceSaved = invoiceRepository.save(outstandingInvoice);
        invoiceReference = outstandingInvoiceSaved.getReference();
    }

    @Test
    public void givenInvoice_whenPostFindInvoice_thenStatus200() throws Exception {
        mvc.perform(post("/portal/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reference\": \"" + invoiceReference + "\"}")
                        .flashAttr("invoice", outstandingInvoice))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString(invoiceReference)));
    }

    @Test
    public void givenNoInvoice_whenPostFindInvoice_thenStatus404() throws Exception {
        outstandingInvoice.setReference("BBBB9999");
        mvc.perform(post("/portal/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reference\": \"BBBB9999\"}")
                        .flashAttr("invoice", outstandingInvoice))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Could not find invoice for reference BBBB9999"));
    }

    @Test
    public void givenInvoice_whenPostPayInvoice_thenStatus200_andInvoicePaid() throws Exception {
        mvc.perform(post("/portal/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("invoice", outstandingInvoice))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("PAID")));
    }

    @Test
    public void givenInvoiceWithNullReference_whenPostPayInvoice_thenStatus404() throws Exception {
        outstandingInvoice.setReference(null);
        mvc.perform(post("/portal/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("invoice", outstandingInvoice))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Could not find invoice."));
    }

    @Test
    public void givenInvoiceWithNonExistingReference_whenPostPayInvoice_thenStatus404() throws Exception {
        outstandingInvoice.setReference("XXXXXXXX");
        mvc.perform(post("/portal/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("invoice", outstandingInvoice))
                .andExpect(status().isNotFound())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Could not find invoice for reference XXXXXXXX"));
    }

    @Test
    public void whenGetPortal_thenStatus200() throws Exception {
        mvc.perform(get("/portal")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Invoice Payment Portal")));
    }

    @Test
    public void whenGetPortalInvoice_thenStatus200() throws Exception {
        mvc.perform(get("/portal/invoice")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Invoice Payment Portal")));
    }

    @Test
    public void whenGetRoot_thenRedirectToPortal_AndStatus302() throws Exception {
        mvc.perform(get("/")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/portal"));
    }

    @AfterEach
    public void tearDown() {
        invoiceRepository.deleteAll();
    }
}
