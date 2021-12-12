package uk.ac.leedsbeckett.finance.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.leedsbeckett.finance.exception.InvoiceNotValidException;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class InvoiceModelAssemblerTest {
    private final LocalDate dueDate = LocalDate.of(2021, Month.DECEMBER, 25);
    private final Type invoiceType = Type.TUITION_FEES;
    private Invoice invoice;

    @Autowired
    private InvoiceModelAssembler invoiceModelAssembler;

    @BeforeEach
    public void setUp() {
        String studentId = "c7777777";
        Long accountId = 1L;
        Double amount = 10.5;
        Long invoiceId = 1L;
        Account account = new Account(studentId);
        invoice = new Invoice(amount, dueDate, invoiceType, account);
        account.setId(accountId);
        invoice.setId(invoiceId);
    }

    @Test
    void testToModel_withValidPaidInvoice_ReturnsExpectedEntityModel() {
        invoice.setStatus(Status.PAID);
        EntityModel<Invoice> result = invoiceModelAssembler.toModel(invoice);
        assertThat(invoice.equals(result.getContent()));
        assertThat(result.getLinks().hasSize(2));
        assertThat(result.hasLink("http://localhost/invoices/1"));
        assertThat(result.hasLink("http://localhost/invoices"));
    }

    @Test
    void testToModel_withValidCancelledInvoice_ReturnsExpectedEntityModel() {
        invoice.setStatus(Status.CANCELLED);
        EntityModel<Invoice> result = invoiceModelAssembler.toModel(invoice);
        assertThat(invoice.equals(result.getContent()));
        assertThat(result.getLinks().hasSize(2));
        assertThat(result.hasLink("http://localhost/invoices/1"));
        assertThat(result.hasLink("http://localhost/invoices"));
    }

    @Test
    void testToModel_withValidOutstandingInvoice_ReturnsExpectedEntityModel() {
        invoice.setStatus(Status.OUTSTANDING);
        EntityModel<Invoice> result = invoiceModelAssembler.toModel(invoice);
        assertThat(invoice.equals(result.getContent()));
        assertThat(result.getLinks().hasSize(4));
        assertThat(result.hasLink("http://localhost/invoices/1"));
        assertThat(result.hasLink("http://localhost/invoices"));
        assertThat(result.hasLink("http://localhost/invoices/pay"));
        assertThat(result.hasLink("http://localhost/invoices/cancel"));
    }

    @Test
    void testToModel_withIdNull_ThrowsException() {
        invoice.setId(null);
        assertThrows(InvoiceNotValidException.class, () -> invoiceModelAssembler.toModel(invoice),
                "Exception was not thrown.");
    }

    @Test
    void testToModel_withIdZero_ThrowsException() {
        invoice.setId(0L);
        assertThrows(InvoiceNotValidException.class, () -> invoiceModelAssembler.toModel(invoice),
                "Exception was not thrown.");
    }

    @Test
    void testToModel_withReferenceNull_ThrowsException() {
        invoice.setReference(null);
        assertThrows(InvoiceNotValidException.class, () -> invoiceModelAssembler.toModel(invoice),
                "Exception was not thrown.");
    }

    @Test
    void testToModel_withReferenceEmpty_ThrowsException() {
        invoice.setReference("");
        assertThrows(InvoiceNotValidException.class, () -> invoiceModelAssembler.toModel(invoice),
                "Exception was not thrown.");
    }

    @Test
    void testToModel_withNoDueDate_ThrowsException() {
        invoice.setDueDate(null);
        assertThrows(InvoiceNotValidException.class, () -> invoiceModelAssembler.toModel(invoice),
                "Exception was not thrown.");
    }

    @Test
    void testToModel_withNoAmount_ThrowsException() {
        invoice.setAmount(null);
        assertThrows(InvoiceNotValidException.class, () -> invoiceModelAssembler.toModel(invoice),
                "Exception was not thrown.");
    }

    @Test
    void testToModel_withNullArgument_ThrowsException() {
        assertThrows(RuntimeException.class, () -> invoiceModelAssembler.toModel(null),
                "Exception was not thrown.");
    }

}