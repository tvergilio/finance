package uk.ac.leedsbeckett.finance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.leedsbeckett.finance.system.UserRepository;
import uk.ac.leedsbeckett.finance.system.User;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository repository) {

        return args -> {
            log.info("Preloading " + repository.save(new User("Walter White", "teacher")));
            log.info("Preloading " + repository.save(new User("Jesse Pinkman", "admin")));
        };
    }
}