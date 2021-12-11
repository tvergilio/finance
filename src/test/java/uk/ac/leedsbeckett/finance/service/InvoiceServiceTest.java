package uk.ac.leedsbeckett.finance.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.leedsbeckett.finance.controller.InvoiceController;
import uk.ac.leedsbeckett.finance.exception.InvoiceNotFoundException;
import uk.ac.leedsbeckett.finance.model.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class InvoiceServiceTest {

    @TestConfiguration
    class InvoiceServiceImplTestContextConfiguration {
        @Bean
        public InvoiceService invoiceService() {
            return new InvoiceService(accountRepository, invoiceModelAssembler, invoiceRepository);
        }
    }

    private final LocalDate dueDate = LocalDate.of(2021, Month.DECEMBER, 25);
    private final LocalDate anotherDueDate = LocalDate.of(2018, Month.NOVEMBER, 23);
    private final Type type = Type.TUITION_FEES;
    private final Type anotherType = Type.LIBRARY_FINE;
    private final Long invoiceId = 1L;
    private Account account;
    private Account anotherAccount;
    private Invoice invoice;
    private Invoice anotherInvoice;

    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private InvoiceRepository invoiceRepository;
    @SpyBean
    private InvoiceModelAssembler invoiceModelAssembler;
    @Autowired
    private InvoiceService invoiceService;

    @BeforeEach
    public void setUp() {
        final String studentId = "c7777777";
        final String anotherStudentId = "c3333333";
        final Long accountId = 1L;
        final Long anotherAccountId = 2L;
        final Long anotherInvoiceId = 2L;
        final Double amount = 10.5;
        final Double anotherAmount = 20.99;
        account = new Account(studentId);
        account.setId(accountId);
        anotherAccount = new Account(anotherStudentId);
        anotherAccount.setId(anotherAccountId);
        invoice = new Invoice(amount, dueDate, type, account);
        invoice.setId(invoiceId);
        anotherInvoice = new Invoice(anotherAmount, anotherDueDate, anotherType, anotherAccount);
        anotherInvoice.setId(anotherInvoiceId);
        Mockito.when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.of(invoice));
        Mockito.when(invoiceRepository.findAll())
                .thenReturn(Arrays.asList(invoice, anotherInvoice));
        Mockito.when(invoiceRepository.save(invoice))
                .thenReturn(invoice);
        Mockito.when(accountRepository.findAccountByStudentId(studentId))
                .thenReturn(account);
        Mockito.doNothing().when(invoiceRepository).deleteById(isA(Long.class));
    }

    @Test
    void testGetInvoiceById_withValidID_ReturnsExistingInvoice() {
        EntityModel<Invoice> result = invoiceService.getInvoiceById(invoiceId);
        assertEquals(invoiceId, result.getContent().getId());
        verify(invoiceModelAssembler, times(1)).toModel(invoice);
    }

    @Test
    void testGetInvoiceById_withInValidID_throwsException() {
        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.getInvoiceById(0L),
                "Exception was not thrown.");
        verify(invoiceModelAssembler, times(0)).toModel(any());
    }

    @Test
    void testGetInvoiceById_withNullID_throwsException() {
        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.getInvoiceById(null),
                "Exception was not thrown.");
        verify(invoiceModelAssembler, times(0)).toModel(any());
    }

    @Test
    void testGetAllInvoices_returnsExistingInvoices() {
        CollectionModel<EntityModel<Invoice>> result = invoiceService.getAllInvoices();
        assertEquals(2, result.getContent().size());
        assertThat(result.getContent().containsAll(Arrays.asList(invoice, anotherInvoice)));
        verify(invoiceModelAssembler, times(1)).toModel(invoice);
        verify(invoiceModelAssembler, times(1)).toModel(anotherInvoice);
    }

    @Test
    void testCreateNewInvoice_withValidData_createsInvoice() {
        EntityModel<Invoice> invoiceEntityModel = EntityModel.of(invoice,
                linkTo(methodOn(InvoiceController.class).one(invoice.getId())).withSelfRel(),
                linkTo(methodOn(InvoiceController.class).all()).withRel("invoices"),
                linkTo(methodOn(InvoiceController.class).cancel(invoice.getId())).withRel("cancel"),
                linkTo(methodOn(InvoiceController.class).pay(invoice.getId())).withRel("pay"));
        assertEquals(invoiceEntityModel, invoiceService.createNewInvoice(invoice).getBody());
        verify(invoiceModelAssembler, times(1)).toModel(invoice);
    }

    @Test
    void testCancelInvoice_withValidId_CancelsInvoice() {
        invoice.setStatus(Status.OUTSTANDING);
        ResponseEntity<?> result = invoiceService.cancel(invoiceId);
        invoice.setStatus(Status.CANCELLED);
        EntityModel<Invoice> invoiceEntityModel = EntityModel.of(invoice,
                linkTo(methodOn(InvoiceController.class).one(invoice.getId())).withSelfRel(),
                linkTo(methodOn(InvoiceController.class).all()).withRel("invoices"));
        assertEquals(invoiceEntityModel, result.getBody());
        verify(invoiceModelAssembler, times(1)).toModel(invoice);
    }

    @Test
    void testPayInvoice_withValidId_CancelsInvoice() {
        invoice.setStatus(Status.OUTSTANDING);
        ResponseEntity<?> result = invoiceService.pay(invoiceId);
        invoice.setStatus(Status.PAID);
        EntityModel<Invoice> invoiceEntityModel = EntityModel.of(invoice,
                linkTo(methodOn(InvoiceController.class).one(invoice.getId())).withSelfRel(),
                linkTo(methodOn(InvoiceController.class).all()).withRel("invoices"));
        assertEquals(invoiceEntityModel, result.getBody());
        verify(invoiceModelAssembler, times(1)).toModel(invoice);
    }

    @AfterEach
    public void tearDown() {
        account = null;
        anotherAccount = null;
        invoice = null;
        anotherInvoice = null;
    }
}