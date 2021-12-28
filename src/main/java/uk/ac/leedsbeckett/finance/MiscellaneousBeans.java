package uk.ac.leedsbeckett.finance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import uk.ac.leedsbeckett.finance.model.*;

import java.util.Locale;

@Configuration
class MiscellaneousBeans {

    private static final Logger log = LoggerFactory.getLogger(MiscellaneousBeans.class);

    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource
                = new ReloadableResourceBundleMessageSource();

        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
        sessionLocaleResolver.setDefaultLocale(Locale.UK);
        return sessionLocaleResolver;
    }

    @Bean
    CommandLineRunner initDatabase(InvoiceRepository invoiceRepository, AccountRepository accountRepository) {
        return args -> {
//            Account account1 = new Account("c7465688");
//            Account account2 = new Account("c3608824");
//
//            accountRepository.save(account1);
//            accountRepository.save(account2);
//
//            invoiceRepository.save(new Invoice(350.40, LocalDateTime.now().plusDays(30), Type.TUITION_FEES, Status.OUTSTANDING, account1));
//            invoiceRepository.save(new Invoice(810.50, LocalDateTime.now().minusMonths(2), Type.TUITION_FEES, Status.PAID, account2));
//            invoiceRepository.save(new Invoice(37.85, LocalDateTime.now().minusMonths(1), Type.LIBRARY_FINE, Status.CANCELLED, account1));
//            invoiceRepository.save(new Invoice(1000.0, LocalDateTime.now().minusMonths(9), Type.TUITION_FEES, Status.PAID, account1));
//            invoiceRepository.save(new Invoice(15.60, LocalDateTime.now().plusDays(15), Type.LIBRARY_FINE, Status.OUTSTANDING, account2));
//
//            invoiceRepository.findAll().forEach(invoice -> log.info("Preloaded " + invoice));
//            accountRepository.findAll().forEach(account -> log.info("Preloaded " + account));

        };
    }
}