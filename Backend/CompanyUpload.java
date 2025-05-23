package onetoone.HomePage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.util.JSONPObject;
import jakarta.persistence.*;
import lombok.Data;
import onetoone.Company.Company;
import onetoone.Notifications.Notifications;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "company_upload")
public class CompanyUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String postTitle;
    private String postDescription;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private String imageData; // sending a byte array to implement BASE64
    //websocket implmentation
    private int likeCount;
    private int commentCount;
    @Column(unique = true)
    private String postId;
    private boolean isLiked;
//    @ElementCollection
//    private List<String> comments;


    //    @ManyToMany
//    @JoinTable(
//        name = "companyPostID",
//            joinColumns = @JoinColumn(name = "post_id"),
//            inverseJoinColumns = @JoinColumn(name = "companyName")
//    )
//    private Set<Company> postID = new HashSet<>();
//
    @ManyToOne
    @JoinColumn(name = "company_name", referencedColumnName = "companyName")
    private Company company;

    @OneToMany(mappedBy = "companyUpload", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Interaction> interactions = new ArrayList<>();

    @OneToMany(mappedBy = "companyUpload", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notifications> notifications = new ArrayList<>();

    public CompanyUpload(String postTitle, String postDescription,
                         String imageData, int likeCount, int commentCount,
                         String postId, boolean isLiked, Company company) {
        this.postTitle = postTitle;
        this.imageData = imageData;
        this.postDescription = postDescription;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.postId = postId;
        this.isLiked = isLiked;
        this.company = company;
    }

    public CompanyUpload() {
    }

    public String getUploadCompanyName() {
        return (company != null) ? company.getCompanyName() : null;
    }

    public void setUploadCompanyName(Company companyName) {
        this.company = companyName;
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

    public String getPostID() {
        return postId;
    }

    public void setPostID(String postId) {
        this.postId = postId;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getDescription() {
        return postDescription;
    }

    public void setDescription(String description) {
        this.postDescription = description;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

}
