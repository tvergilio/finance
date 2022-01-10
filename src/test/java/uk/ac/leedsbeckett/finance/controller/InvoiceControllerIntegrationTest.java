package uk.ac.leedsbeckett.finance.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.leedsbeckett.finance.model.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureRestDocs(outputDir = "build/snippets")
public class InvoiceControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private String outstandingInvoiceReference;
    private String paidInvoiceReference;
    private String cancelledInvoiceReference;
    private List<FieldDescriptor> invoiceResponseFieldDescriptor;
    private List<LinkDescriptor> linkDescriptors;

    @BeforeEach
    public void setUp() {
        Account account = new Account("c6666666");
        account.setId(1L);
        accountRepository.save(account);
        Invoice outstandingInvoice = new Invoice(10.50, LocalDate.of(2021, Month.DECEMBER, 25), Type.TUITION_FEES, account);
        outstandingInvoice.setStatus(Status.OUTSTANDING);
        Invoice outstandingInvoiceSaved = invoiceRepository.save(outstandingInvoice);
        outstandingInvoiceReference = outstandingInvoiceSaved.getReference();
        Invoice paidInvoice = new Invoice(5.30, LocalDate.of(2022, Month.JANUARY, 10), Type.LIBRARY_FINE, account);
        paidInvoice.setStatus(Status.PAID);
        Invoice paidInvoiceSaved = invoiceRepository.save(paidInvoice);
        paidInvoiceReference = paidInvoiceSaved.getReference();
        Invoice cancelledInvoice = new Invoice(1.00, LocalDate.of(2022, Month.FEBRUARY, 28), Type.LIBRARY_FINE, account);
        cancelledInvoice.setStatus(Status.CANCELLED);
        Invoice cancelledInvoiceSaved = invoiceRepository.save(cancelledInvoice);
        cancelledInvoiceReference = cancelledInvoiceSaved.getReference();
        configureRestDocumentation();
    }

    @Test
    public void a_givenInvoice_whenGetInvoiceById_thenStatus200() throws Exception {
        mvc.perform(get("/invoices/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.studentId").value("c6666666"))
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    public void givenInvoice_whenDelete_thenStatus200_andInvoiceCancelled() throws Exception {
        mvc.perform(delete("/invoices/{reference}/cancel", outstandingInvoiceReference)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.reference").value(outstandingInvoiceReference))

                .andDo(document("delete-invoice",
                        pathParameters(parameterWithName("reference").description("The external reference of the invoice to delete.")),
                        links(linkWithRel("self").description("Link to this resource."),
                                linkWithRel("invoices").description("Link to all invoices.")),
                        responseFields(invoiceResponseFieldDescriptor)));
    }

    @Test
    public void givenOutstandingInvoice_whenPay_thenStatus200_andInvoicePaid() throws Exception {
        mvc.perform(put("/invoices/{reference}/pay", outstandingInvoiceReference)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.reference").value(outstandingInvoiceReference))

                .andDo(document("pay-invoice",
                pathParameters(parameterWithName("reference").description("The external reference of the invoice to pay.")),
                links(linkWithRel("self").description("Link to this resource."),
                        linkWithRel("invoices").description("Link to all invoices.")),
                responseFields(invoiceResponseFieldDescriptor)));
    }

    @Test
    public void givenPaidInvoice_whenPay_thenStatus405() throws Exception {
        mvc.perform(put("/invoices/{reference}/pay", paidInvoiceReference)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Method not allowed"))
                .andExpect(jsonPath("$.detail").value("You can't pay an invoice that is in the PAID status"));
    }

    @Test
    public void givenCancelledInvoice_whenPay_thenStatus405() throws Exception {
        mvc.perform(put("/invoices/{reference}/pay", cancelledInvoiceReference)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Method not allowed"))
                .andExpect(jsonPath("$.detail").value("You can't pay an invoice that is in the CANCELLED status"));
    }

    @Test
    public void givenInvoices_whenGetInvoices_thenStatus200() throws Exception {
        mvc.perform(get("/invoices")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.invoiceList[0].reference").value(outstandingInvoiceReference))
                .andExpect(jsonPath("$._embedded.invoiceList[1].reference").value(paidInvoiceReference))
                .andExpect(jsonPath("$._embedded.invoiceList[2].reference").value(cancelledInvoiceReference))

                .andDo(document("get-invoices",
                        links(halLinks(), linkWithRel("self").description("Link to this resource.")),
                        responseFields(fieldWithPath("_embedded.invoiceList[]").description("A list of all invoices in the system."),
                                subsectionWithPath("_links").ignored())
                                .andWithPrefix("_embedded.invoiceList[].", invoiceResponseFieldDescriptor)));
    }

    @Test
    public void givenNoInvoices_whenGetInvoices_thenStatus200_andLinkToSelf() throws Exception {
        invoiceRepository.deleteAll();
        mvc.perform(get("/invoices")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._links.self.href").isNotEmpty());
    }

    @Test
    public void givenNoInvoice_whenGetInvoiceById_thenStatus404() throws Exception {
        mvc.perform(get("/invoices/1000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenInvoice_whenGetInvoiceByReference_thenStatus200() throws Exception {
        mvc.perform(get("/invoices/reference/{ref}", outstandingInvoiceReference)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.reference").value(outstandingInvoiceReference))

                .andDo(document("get-invoice-by-reference",
                        pathParameters(parameterWithName("ref").description("The external reference of the invoice to return.")),
                        links(linkDescriptors),
                        responseFields(invoiceResponseFieldDescriptor)));
    }

    @Test
    public void givenNoInvoice_whenGetInvoiceByReference_thenStatus404() throws Exception {
        mvc.perform(get("/invoices/reference/1000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenNoInvoice_whenPostNewInvoice_thenStatus201() throws Exception {
        mvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 15.00, \"dueDate\": \"2021-11-06\",\"type\": \"LIBRARY_FINE\",\"account\": {\"studentId\": \"c6666666\"}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value("c6666666"))
                .andExpect(jsonPath("$.status").value("OUTSTANDING"))

                .andDo(document("create-invoice",
                requestFields(fieldWithPath("amount").description("The invoice amount."),
                        fieldWithPath("dueDate").description("The date the invoice must be paid by (yyyy-MM-dd)."),
                        fieldWithPath("type").description("The type of invoice. Types currently enabled are: LIBRARY_FINE and TUITION_FEES."),
                        fieldWithPath("account.studentId").description("The external ID of the student responsible for paying this invoice.")),
                links(linkDescriptors),
                responseFields(invoiceResponseFieldDescriptor)));
    }

    @Test
    public void whenPostNewInvoice_withEmptyStudentIdValue_thenStatus422() throws Exception {
        mvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 15.00, \"dueDate\": \"2021-11-06\",\"type\": \"LIBRARY_FINE\",\"account\": {\"studentId\": \"\"}}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void whenPostNewInvoice_withEmptyAccountValue_thenStatus422() throws Exception {
        mvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 15.00, \"dueDate\": \"2021-11-06\",\"type\": \"LIBRARY_FINE\",\"account\": \"\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void whenPostNewInvoice_withEmptyJson_thenStatus400() throws Exception {
        mvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoInvoice_whenDelete_thenStatus404() throws Exception {
        mvc.perform(delete("/invoices/999999/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Could not find invoice for reference 999999"));
    }

    @Test
    public void givenNoInvoice_whenPay_thenStatus405() throws Exception {
        mvc.perform(delete("/invoices/999999/pay")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    private void configureRestDocumentation() {
        invoiceResponseFieldDescriptor = List.of(
                fieldWithPath("id").description("The internal ID of the invoice."),
                fieldWithPath("reference").description("The external invoice reference used to pay an invoice via the Payment Portal."),
                fieldWithPath("amount").description("The invoice amount."),
                fieldWithPath("dueDate").description("The date the invoice must be paid by."),
                fieldWithPath("type").description("The type of invoice. Types currently enabled are: LIBRARY_FINE and TUITION_FEES."),
                fieldWithPath("status").description("The status of the invoice: OUTSTANDING, PAID or CANCELLED."),
                fieldWithPath("studentId").description("The external ID of the student responsible for paying this invoice."),
                subsectionWithPath("_links").ignored()
        );
        linkDescriptors = List.of(linkWithRel("self").description("Link to this resource."),
                linkWithRel("invoices").description("Link to all invoices."),
                linkWithRel("cancel").description("Link to cancel this invoice."),
                linkWithRel("pay").description("Link to pay this invoice."));
    }

    @AfterEach
    public void tearDown() {
        invoiceRepository.deleteAll();
    }
}
