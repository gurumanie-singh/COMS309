package onetoone.Degrees;

import jakarta.persistence.*;
import onetoone.Users.User;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "degrees")
public class Degrees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String degreeName;

    @ManyToMany(mappedBy = "degrees")
    private Set<User> users = new HashSet<>();

    public Degrees(String degreeName) {
        this.degreeName = degreeName;
    }

    public Degrees() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDegreeName() {
        return degreeName;
    }

    public void setDegreeName(String degreeName) {
        this.degreeName = degreeName;
    }

    @Override
    public String toString() {
        return "Degree{" +
                "title='" + degreeName + '\'' +
                '}';
    }
}
