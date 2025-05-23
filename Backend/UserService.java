package onetoone.Users;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createClownUser() {
        // Check if the "Clown" user already exists
        User existingUser = userRepository.findByUsername("Clown");
        if (existingUser != null) {
            throw new IllegalStateException("The Clown user already exists!");
        }

        // Random details generator
        Random random = new Random();
        String[] funnyFirstNames = {"Bozo", "Bubbles", "Giggles", "Chuckles", "Snickers"};
        String[] funnyLastNames = {"McJester", "Balloono", "Funnybone", "Bigshoe", "Wiggly"};
        String[] funnyJobTitles = {"Pie Thrower", "Balloon Artist", "Unicyclist Extraordinaire", "Comedian"};
        String[] funnyCountries = {"Clownland", "Circus Kingdom", "Giggle Nation", "Funnyville"};

        User clownUser = new User();
        clownUser.setUsername("Clown");
        clownUser.setUserFirstName(funnyFirstNames[random.nextInt(funnyFirstNames.length)]);
        clownUser.setUserLastName(funnyLastNames[random.nextInt(funnyLastNames.length)]);
        clownUser.setUserEmail("clown@" + clownUser.getUserLastName().toLowerCase() + ".com");
        clownUser.setUserPassword("funnyPassword"); // Note: Hash it with BCrypt if necessary
        clownUser.setUserGender("Non-binary");
        clownUser.setUserBirthDate("01-01-1900");
        clownUser.setUserJobTitle(funnyJobTitles[random.nextInt(funnyJobTitles.length)]);
        clownUser.setUserCountry(funnyCountries[random.nextInt(funnyCountries.length)]);
        clownUser.setUserMajor("Clownology");
        clownUser.setUserPhoneNumber(random.nextInt(999999999)); // Random phone number
        clownUser.setIfActive(true);
        clownUser.setIsAdmin(false);

        // Save the Clown user in the database
        return userRepository.save(clownUser);
    }
}
