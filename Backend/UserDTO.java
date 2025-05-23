package onetoone.Users;
import java.util.Set;

public class UserDTO {
    private Long id;
    private String username;
    private Set<String> friends; // You can also use List<String> or other types as needed

    // Constructor
    public UserDTO(Long id, String username, Set<String> friends) {
        this.id = id;
        this.username = username;
        this.friends = friends;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getFriends() {
        return friends;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFriends(Set<String> friends) {
        this.friends = friends;
    }
}
