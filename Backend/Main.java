package onetoone;

import onetoone.Company.Company;
import onetoone.Company.CompanyRepository;
//import onetoone.HomePage.CompanyUploadRepository;
import onetoone.Messages.MessageRepository;
import onetoone.Users.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import onetoone.Messages.MessageRepository;
import onetoone.Users.UserRepository;

import java.util.List;

/**
 * @author Vivek Bengre
 */

@SpringBootApplication
@EnableAsync  // Enable async processing
@EnableJpaRepositories
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }


    /**
     * @param userRepository    repository for the User entity
     * @param companyRepository repository for the Laptop entity
     *                          Creates a commandLine runner to enter dummy data into the database
     *                          As mentioned in User.java just associating the Laptop object with the User will save it into the database because of the CascadeType
     */
    @Bean
    CommandLineRunner initUser(UserRepository userRepository, CompanyRepository companyRepository, MessageRepository messageRepository) {
        return args -> {

        };
    }
}
