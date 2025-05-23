package onetoone.Users;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import onetoone.CareerClusters.CareerClusters;
import onetoone.Degrees.Degrees;
import onetoone.HomePage.Interaction;
import onetoone.Notifications.Notifications;
import onetoone.University.University;
import onetoone.ProfilePhoto.Image;
//import onetoone.UserName.UserName;

import javax.management.Notification;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Gurumanie Singh
 */

//@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "user_details")
public class User {
    /*
     * The annotation @ID marks the field below as the primary key for the table created by springboot
     * The @GeneratedValue generates a value if not already present, The strategy in this case is to start from 1 and increment for each table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String userFirstName;
    private String userMiddleName; //optional
    private String userLastName;
    private String userEmail;
    private String userPassword;
    private boolean isAdmin;
    private boolean ifActive;
    private int userPhoneNumber;
    private String userGender;
    private String userBirthDate;
    private String userMajor;
    private String userJobTitle;
    private String userJobCompany;
    private String userJobDuration;
    private String userCountry;

    /*
     * @OneToOne creates a relation between the current entity/table(Laptop) with the entity/table defined below it(User)
     * cascade is responsible propagating all changes, even to children of the class Eg: changes made to laptop within a user object will be reflected
     * in the database (more info : https://www.baeldung.com/jpa-cascade-types)
     * @JoinColumn defines the ownership of the foreign key i.e. the user table will have a field called laptop_id
     */
    @ManyToMany
    @JoinTable(
            name = "user_degrees",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "degree_id")
    )
    private Set<Degrees> degrees = new HashSet<>();

    @ManyToOne
    private University university;

    @ManyToMany
    @JoinTable(
            name = "user_career_clusters",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "career_cluster_id")
    )
    private Set<CareerClusters> careerClusters = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_picture_id", referencedColumnName = "id")
    @JsonManagedReference // Prevents recursion during serialization
    private Image profilePicture;

    @OneToMany(mappedBy = "username", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Interaction> interactions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notifications> notifications = new ArrayList<>();


    public User(String username, String userFirstName, String userMiddleName, String userLastName,
                String userEmail, String userPassword,
                String userGender, String userBirthDate, String userMajor,
                String userCountry, int userPhoneNumber, String userJobTitle, String userJobCompany,
                String userJobDuration, Boolean isAdmin) {
        this.username = username;
        this.userFirstName = userFirstName;
        this.userMiddleName = userMiddleName;
        this.userLastName = userLastName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.isAdmin = false;
        this.ifActive = true;
        this.userPhoneNumber = userPhoneNumber;
        this.userMajor = userMajor;
        this.userJobTitle = userJobTitle;
        this.userJobCompany = userJobCompany;
        this.userJobDuration = userJobDuration;
        this.userGender = userGender;
        this.userBirthDate = userBirthDate;
        this.userCountry = userCountry;

    }

    public User() {
    }

    // =============================== Getters and Setters for each field ================================== //
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean getIfActive() {
        return ifActive;
    }

    public void setIfActive(boolean ifActive) {
        this.ifActive = ifActive;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String firstName) {
        this.userFirstName = firstName;
    }

    public String getUserMiddleName() {
        return userMiddleName;
    }

    public void setUserMiddleName(String middleName) {
        this.userMiddleName = middleName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String lastName) {
        this.userLastName = lastName;
    }

    public int getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(int phoneNumber) {
        this.userPhoneNumber = phoneNumber;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String password) {
        this.userPassword = password;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String gender) {
        this.userGender = gender;
    }

    public String getUserBirthDate() {
        return userBirthDate;
    }

    public void setUserBirthDate(String userBirthDate) {
        this.userBirthDate = userBirthDate;
    }

    public String getUserJobTitle() {
        return userJobTitle;
    }

    public void setUserJobTitle(String jobTitle) {
        this.userJobTitle = jobTitle;
    }

    public String getUserCountry() {
        return userCountry;
    }

    public void setUserCountry(String country) {
        this.userCountry = country;
    }

    public String getUserMajor() {
        return userMajor;
    }

    public void setUserMajor(String major) {
        this.userMajor = major;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public String getUserJobCompany() {
        return userJobCompany;
    }

    public void setUserJobCompany(String jobCompany) {
        this.userJobCompany = jobCompany;
    }

    public String getUserJobDuration() {
        return userJobDuration;
    }

    public void setUserJobDuration(String jobDuration) {
        this.userJobDuration = jobDuration;
    }

    public University getUniversity() {
        return university;
    }

    public void setUniversity(University university) {
        this.university = university;
    }

    public Set<CareerClusters> getCareerClusters() {
        return careerClusters;
    }

    public void setCareerClusters(Set<CareerClusters> careerClusters) {
        this.careerClusters = careerClusters;
    }

    public Set<Degrees> getDegrees() {
        return degrees;
    }

    public void setDegrees(Set<Degrees> degrees) {
        this.degrees = degrees;
    }

    @JsonBackReference // This will prevent serialization of the back-reference
    public Set<User> getFriends() {
        return friends;
    }

    public void setFriends(Set<User> friends) {
        this.friends = friends;
    }

    public void addFriend(User friend) {
        if (friend != null && !this.friends.contains(friend)) {
            this.friends.add(friend);
            friend.addFriend(this); // Ensure bidirectional relationship
        }
    }

    public void removeFriend(User friend) {
        if (friend != null && this.friends.contains(friend)) {
            this.friends.remove(friend);
            if (friend.getFriends().contains(this)) {
                friend.removeFriend(this); // Only call if the friend still has this user in their set
            }
        }
    }

    public Image getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Image profilePicture) {
        this.profilePicture = profilePicture;
    }

    public List<Interaction> getInteractions() {
        return interactions;
    }

    public void setInteractions(List<Interaction> interactions) {
        this.interactions = interactions;
    }

    public List<Notifications> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notifications> notifications) {
        this.notifications = notifications;
    }


}
