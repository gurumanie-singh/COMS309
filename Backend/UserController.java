package onetoone.Users;

import java.util.*;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.CareerClusters.CareerClusters;
import onetoone.CareerClusters.CareerClustersRepository;
import onetoone.Degrees.Degrees;
import onetoone.Degrees.DegreesRepository;
import onetoone.University.University;
import onetoone.University.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * @author Gurumanie Singh
 */
@RestController
@Tag(name = "User Controller", description = "REST APIs related to User Entity")
public class UserController {

    @Autowired
    UserRepository userRepository;
s
    @Autowired
    CareerClustersRepository careerClustersRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private DegreesRepository degreesRepository;

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @Operation(summary = "Get all users", description = "Retrieve a list of all users from the database.")
    @GetMapping(path = "/users")
    List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Operation(summary = "Get user by ID", description = "Retrieve a user by their unique ID.")
    @GetMapping(path = "/users/{id}")
    User getUserById(@PathVariable int id) {
        return userRepository.findById(id);
    }

    @Operation(summary = "Get user's friends", description = "Retrieve the list of friends associated with a user.")
    @GetMapping("/{username}/friends")
    public ResponseEntity<Set<User>> getFriends(@PathVariable String username) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Set<User> friends = user.getFriends();
        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    @Operation(summary = "Get user by username", description = "Retrieve a user's details by their username.")
    @GetMapping("/users/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("isExist", false);
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);  // Return the full User object
    }

    @Operation(summary = "Get users by university name", description = "Retrieve a list of users associated with a specific university.")
    @GetMapping("/users/university/{universityName}")
    public ResponseEntity<List<User>> getUsersByUniversityName(@PathVariable String universityName) {
        University university = universityRepository.findByUniversityName(universityName);

        if (university == null) {
            return ResponseEntity.noContent().build();
        }

        List<User> users = userRepository.findByUniversity(university);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get users by degree name", description = "Retrieve a list of users with a specific degree.")
    @GetMapping("/users/degree/{degreeName}")
    public ResponseEntity<List<User>> getUsersByDegreeName(@PathVariable String degreeName) {
        Degrees degree = degreesRepository.findByDegreeName(degreeName);

        if (degree == null) {
            return ResponseEntity.noContent().build();
        }

        List<User> users = userRepository.findByDegrees(degree);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get users by career cluster", description = "Retrieve a list of users belonging to a specific career cluster.")
    @GetMapping("/users/careerCluster/{clusterName}")
    public ResponseEntity<List<User>> getUsersByCareerCluster(@PathVariable String clusterName) {
        CareerClusters careerCluster = careerClustersRepository.findByClusterName(clusterName);

        if (careerCluster == null) {
            return ResponseEntity.noContent().build(); // Return 204 No Content if not found
        }

        List<User> users = userRepository.findByCareerClusters(careerCluster);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get friend suggestions", description = "Suggest friends for a user based on mutual friends, university, or interests.")
    @GetMapping("/{username}/friendSuggestions")
    public ResponseEntity<Set<User>> getFriendSuggestions(@PathVariable String username) {
        // Step 1: Retrieve the user
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Step 2: Initialize a set for suggestions
        Set<User> suggestions = new HashSet<>();

        // Step 3: Add suggestions based on mutual friends
        for (User friend : user.getFriends()) {
            suggestions.addAll(friend.getFriends()); // Add friends of friends
        }

        // Step 4: Add suggestions based on shared university
        University userUniversity = user.getUniversity();
        if (userUniversity != null) {
            suggestions.addAll(userRepository.findByUniversity(userUniversity));
        }

        // Step 5: Add suggestions based on shared career clusters
        Set<CareerClusters> userClusters = user.getCareerClusters();
        if (userClusters != null) {
            for (CareerClusters cluster : userClusters) {
                suggestions.addAll(userRepository.findByCareerClusters(cluster));
            }
        }

        // Step 6: Remove the user themselves and their existing friends from suggestions
        suggestions.remove(user);
        suggestions.removeAll(user.getFriends());

        // Step 7: Limit the number of suggestions (optional)
        int maxSuggestions = 10;
        List<User> limitedSuggestions = suggestions.stream()
                .limit(maxSuggestions)
                .collect(Collectors.toList());

        return new ResponseEntity<>(new HashSet<>(limitedSuggestions), HttpStatus.OK);
    }



    @Operation(summary = "Post a Clown user", description = "Creates a clown username with random other fields.")
    @PostMapping("/easter-egg/clown")
    public User createClownUser() {
        return userService.createClownUser();
    }

    @Operation(summary = "Create a new user", description = "Add a new user to the database.")
    @PostMapping(path = "/users")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"User cannot be null\"}");
        }

        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"message\":\"Username already exists\"}");
        }

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("{\"message\":\"User created successfully\"}");
    }

    @Operation(summary = "Check username and ID availability", description = "Verify if a username and ID are available for a new user.")
    @PostMapping(path = "/users/checkid")
    boolean checkUserNameID(@RequestBody User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null || user.getUsername().isEmpty()) {
            return false;
        }
        return true;
    }

    /*
    Checks the login page and will check if username and passcode are stored in the database.
     */
    @Operation(summary = "User login", description = "Authenticate a user by their username and password.")
    @PostMapping(path = "/users/login/{username}")
    ResponseEntity<Map<String, Object>> loginUser(@RequestBody User user) {

        Map<String, Object> body = new HashMap<>();
        try {
            User existingUser = userRepository.findByUsername(user.getUsername());
            if (existingUser == null) {
                body.put("result", false);
                return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
            }
            if (!existingUser.getUserPassword().equals(user.getUserPassword())) {
                body.put("result", false);
                return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
            }
            String firstName = existingUser.getUserFirstName();
            body.put("userFirstName", firstName);
            body.put("userName", existingUser.getUsername());
            body.put("result", true);
            return new ResponseEntity<>(body, HttpStatus.OK);

        } catch (Exception e) {
            body.put(failure, e.getMessage());
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Link user to career clusters", description = "Assign up to 3 career clusters to a user.")
    @PostMapping("/users/careerClusters/{username}")
    public String linkUserToCareerClusters(@PathVariable String username, @RequestBody List<String> careerClusterNames) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return "{\"error\":\"User not found\"}";
        }

        // Retrieve CareerClusters based on the provided names
        List<CareerClusters> careerClusters = careerClustersRepository.findByClusterNameIn(careerClusterNames);

        // Add only up to 3 career clusters
        Set<CareerClusters> currentClusters = user.getCareerClusters();
        int availableSlots = 3 - currentClusters.size();

        if (careerClusters.size() > availableSlots) {
            return "{\"error\":\"User can belong to a maximum of three career clusters.\"}";
        }

        // Add clusters within the limit
        currentClusters.addAll(careerClusters);
        user.setCareerClusters(currentClusters);

        // Save the updated user entity
        userRepository.save(user);

        return "{\"message\":\"User linked to career clusters successfully\"}";
    }

    @Operation(summary = "Add a friend to the user", description = "Add a new friend to the user's friend list.")
    @PostMapping("/{username}/friends/{friendUsername}")
    public ResponseEntity<String> addFriend(@PathVariable String username, @PathVariable String friendUsername) {
        User user = userRepository.findByUsername(username);
        User friend = userRepository.findByUsername(friendUsername);

        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        if (friend == null) {
            return new ResponseEntity<>("Friend not found", HttpStatus.NOT_FOUND);
        }

        // Explicitly cast to avoid ambiguity
        user.addFriend(friend);
        userRepository.save(user);

        return new ResponseEntity<>("Friend added successfully", HttpStatus.OK);
    }

    @Operation(summary = "Assign a university to the user", description = "Assign a specific university to a user.")
    @PostMapping("/users/{username}/university/{universityName}")
    public ResponseEntity<String> assignUniversityToUser(@PathVariable String username, @PathVariable String universityName) {
        User user = userRepository.findByUsername(username);
        University university = universityRepository.findByUniversityName(universityName);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (university == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("University not found");
        }

        user.setUniversity(university);
        userRepository.save(user);

        return ResponseEntity.ok("University assigned to user successfully");
    }

    @Operation(summary = "Assign or replace a degree for the user", description = "Assign or replace a degree for a user.")
    @PostMapping("/users/{username}/degree/{degreeName}")
    public ResponseEntity<String> assignOrReplaceDegreeForUser(@PathVariable String username, @PathVariable String degreeName) {
        User user = userRepository.findByUsername(username);
        Degrees degree = degreesRepository.findByDegreeName(degreeName);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (degree == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Degree not found");
        }

        // Clear existing degrees and add the new degree
        user.getDegrees().clear(); // Clear existing degrees
        user.getDegrees().add(degree); // Add the new degree

        userRepository.save(user);

        return ResponseEntity.ok("User's degree assigned successfully");
    }

    @Operation(summary = "Update a user by their ID", description = "Update a user's details using their unique ID.")
    @PutMapping("/users/{id}")
    User updateUser(@PathVariable int id, @RequestBody User request) {
        User user = userRepository.findById(id);
        if (user == null)
            return null;

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getUserFirstName() != null) {
            user.setUserFirstName(request.getUserFirstName());
        }
        if (request.getUserMiddleName() != null) {
            user.setUserMiddleName(request.getUserMiddleName());
        }
        if (request.getUserLastName() != null) {
            user.setUserLastName(request.getUserLastName());
        }
        if (request.getUserEmail() != null) {
            user.setUserEmail(request.getUserEmail());
        }
        if (request.getUserPassword() != null) {
            user.setUserPassword(request.getUserPassword());
        }
        if (request.getUserBirthDate() != null) {
            user.setUserBirthDate(request.getUserBirthDate());
        }
        if (request.getUserGender() != null) {
            user.setUserGender(request.getUserGender());
        }
        if (request.getUserCountry() != null) {
            user.setUserCountry(request.getUserCountry());
        }
        if (request.getUserMajor() != null) {
            user.setUserMajor(request.getUserMajor());
        }
        if (request.getUserJobCompany() != null) {
            user.setUserJobCompany(request.getUserJobCompany());
        }
        if (request.getUserJobTitle() != null) {
            user.setUserJobTitle(request.getUserJobTitle());
        }
        if (request.getUserJobDuration() != null) {
            user.setUserJobDuration(request.getUserJobDuration());
        }
        userRepository.save(user);
        userRepository.flush();
        return userRepository.findById(id);
    }

    @Operation(summary = "Update a user by their username", description = "Update a user's details using their username.")
    @PutMapping("/users/username/{username}")
    User updateUserName(@PathVariable String username, @RequestBody User request) {
        User userName = userRepository.findByUsername(username);
        if (userName == null)
            return null;

        if (request.getUsername() != null) {
            userName.setUsername(request.getUsername());
        }
        if (request.getUserFirstName() != null) {
            userName.setUserFirstName(request.getUserFirstName());
        }
        if (request.getUserMiddleName() != null) {
            userName.setUserMiddleName(request.getUserMiddleName());
        }
        if (request.getUserLastName() != null) {
            userName.setUserLastName(request.getUserLastName());
        }
        if (request.getUserEmail() != null) {
            userName.setUserEmail(request.getUserEmail());
        }
        if (request.getUserPassword() != null) {
            userName.setUserPassword(request.getUserPassword());
        }
        if (request.getUserBirthDate() != null) {
            userName.setUserBirthDate(request.getUserBirthDate());
        }
        if (request.getUserGender() != null) {
            userName.setUserGender(request.getUserGender());
        }
        if (request.getUserCountry() != null) {
            userName.setUserCountry(request.getUserCountry());
        }
        if (request.getUserMajor() != null) {
            userName.setUserMajor(request.getUserMajor());
        }
        if (request.getUserJobCompany() != null) {
            userName.setUserJobCompany(request.getUserJobCompany());
        }
        if (request.getUserJobTitle() != null) {
            userName.setUserJobTitle(request.getUserJobTitle());
        }
        if (request.getUserJobDuration() != null) {
            userName.setUserJobDuration(request.getUserJobDuration());
        }
        userRepository.save(userName);
        userRepository.flush();
        return userRepository.findByUsername(username);
    }

    @Operation(summary = "Delete user", description = "Delete a user from the database using their ID.")
    @DeleteMapping(path = "/users/{id}")
    String deleteUser(@PathVariable int id) {
        userRepository.deleteById(id);
        return success;
    }

    @Operation(summary = "Remove a friend from the user's friend list", description = "Remove a friend from the user's friend list.")
    @DeleteMapping("/{username}/unfriends/{friendUsername}")
    public ResponseEntity<String> removeFriend(@PathVariable String username, @PathVariable String friendUsername) {
        User user = userRepository.findByUsername(username);
        User friend = userRepository.findByUsername(friendUsername);

        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        if (friend == null) {
            return new ResponseEntity<>("Friend not found", HttpStatus.NOT_FOUND);
        }

        // Explicitly cast to avoid ambiguity
        user.removeFriend(friend);
        userRepository.save(user);

        return new ResponseEntity<>("Friend removed successfully", HttpStatus.OK);
    }

    @Operation(summary = "Unlink specific career clusters from the user", description = "Remove specified career clusters from a user.")
    @DeleteMapping("/users/careerClusters/{username}")
    public String unlinkSpecificCareerClustersFromUser(@PathVariable String username, @RequestBody List<String> careerClusterNames) {
        // Fetch the user
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return "{\"message\":\"User not found\"}";
        }

        // Retrieve the career clusters that match the provided names
        List<CareerClusters> careerClustersToRemove = careerClustersRepository.findByClusterNameIn(careerClusterNames);

        // Remove only the specified clusters from the user's set of career clusters
        user.getCareerClusters().removeAll(careerClustersToRemove);

        // Save the updated user entity to persist the changes
        userRepository.save(user);

        return "{\"message\":\"Specified career clusters unlinked from user successfully\"}";
    }



}


