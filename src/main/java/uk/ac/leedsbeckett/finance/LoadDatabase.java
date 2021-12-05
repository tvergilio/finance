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
    CommandLineRunner initDatabase(UserRepository userRepository, InvoiceRepository invoiceRepository) {
        return args -> {
            userRepository.save(new User("Walter White", "teacher"));
            userRepository.save(new User("Jesse Pinkman", "admin"));
            userRepository.findAll().forEach(user -> log.info("Preloaded " + user));


            invoiceRepository.save(new Invoice(350.40, LocalDateTime.now().plusDays(30), Status.OUTSTANDING));
            invoiceRepository.save(new Invoice(810.50, LocalDateTime.now().minusMonths(2), Status.PAID));
            invoiceRepository.findAll().forEach(invoice -> log.info("Preloaded " + invoice));
        };
    }
}