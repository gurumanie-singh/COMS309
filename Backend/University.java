package onetoone.University;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import onetoone.Users.User;

import java.util.List;

@Entity
@Transactional
@Table(name = "university")
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String universityName;
    private String universityUrl;

    @JsonIgnore
    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL)
    private List<User> users;

    public University(String universityName, String universityUrl, String city, String state, String country) {
        this.universityName = universityName;
        this.universityUrl = universityUrl;
    }

    public University() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void setUser(User user) {
        this.users.add(user);
    }

    public String getUniversityUrl() {
        return universityUrl;
    }

    public void setUniversityUrl(String universityUrl) {
        this.universityUrl = universityUrl;
    }

}
