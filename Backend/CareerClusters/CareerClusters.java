package onetoone.CareerClusters;

import jakarta.persistence.*;
import onetoone.Users.User;

import java.util.List;
import java.util.Set;


/**
 * @author Gurumanie Singh
 */

@Entity
public class CareerClusters {
    /*
     * The annotation @ID marks the field below as the primary key for the table created by springboot
     * The @GeneratedValue generates a value if not already present, The strategy in this case is to start from 1 and increment for each table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int clusterID;
    private String clusterName;
    private String clusterDescription;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "usernameID_id", referencedColumnName = "id")
    private List<CareerClusters> careerCluster;

    /*
     * @OneToOne creates a relation between the current entity/table(Laptop) with the entity/table defined below it(User)
     * cascade is responsible propagating all changes, even to children of the class Eg: changes made to laptop within a user object will be reflected
     * in the database (more info : https://www.baeldung.com/jpa-cascade-types)
     * @JoinColumn defines the ownership of the foreign key i.e. the user table will have a field called laptop_id
     */
    public CareerClusters(int clusterID, String clusterName, String clusterDescription) {
        this.clusterID = clusterID;
        this.clusterName = clusterName;
        this.clusterDescription = clusterDescription;
    }

    public CareerClusters() {
    }

    // =============================== Getters and Setters for each field ================================== //

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClusterID() {
        return clusterID;
    }

    public void setClusterID(int clusterID) {
        this.clusterID = clusterID;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterDescription() {
        return clusterDescription;
    }

    public void setClusterDescription(String clusterDescription) {
        this.clusterDescription = clusterDescription;
    }
}
