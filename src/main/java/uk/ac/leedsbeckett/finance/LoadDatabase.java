package uk.ac.leedsbeckett.finance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.leedsbeckett.finance.model.*;

import java.time.LocalDateTime;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, InvoiceRepository invoiceRepository, AccountRepository accountRepository) {
        return args -> {
            userRepository.save(new User("Walter White", "teacher"));
            userRepository.save(new User("Jesse Pinkman", "admin"));
            userRepository.findAll().forEach(user -> log.info("Preloaded " + user));

            Account account1 = new Account("c7465688");
            Account account2 = new Account("c3608824");

            accountRepository.save(account1);
            accountRepository.save(account2);

            invoiceRepository.save(new Invoice(350.40, LocalDateTime.now().plusDays(30), Status.OUTSTANDING, account1));
            invoiceRepository.save(new Invoice(810.50, LocalDateTime.now().minusMonths(2), Status.PAID, account2));
            invoiceRepository.save(new Invoice(37.85, LocalDateTime.now().minusMonths(1), Status.CANCELLED, account1));
            invoiceRepository.save(new Invoice(1000.0, LocalDateTime.now().minusMonths(9), Status.PAID, account1));
            invoiceRepository.save(new Invoice(15.60, LocalDateTime.now().plusDays(15), Status.OUTSTANDING, account2));

            invoiceRepository.findAll().forEach(invoice -> log.info("Preloaded " + invoice));
            accountRepository.findAll().forEach(account -> log.info("Preloaded " + account));

        };
    }
}