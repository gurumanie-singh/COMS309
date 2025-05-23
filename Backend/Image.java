package onetoone.ProfilePhoto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import onetoone.Users.User;

@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // GenerationType.IDENTITY
    private int id;
    private String filePath;

    @OneToOne(mappedBy = "profilePicture")
    @JsonBackReference // Prevents recursion during serialization
    private User user;


    public Image() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
