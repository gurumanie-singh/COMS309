package onetoone.HomePage;

import aj.org.objectweb.asm.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;
import onetoone.Company.Company;
import onetoone.Users.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
@author Doyle Chism
 */
@Entity
@Table(name = "interactions")
@Data
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    //websocket
    @Column(name = "`like`")
    private boolean like;
    private int likeCount;
    private int commentCount;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username")
    private User username;
    /*
    One username can have many postID interactions
     */
    @ManyToOne
    @JoinColumn(name = "company_upload_id", nullable = false)
    private CompanyUpload companyUpload;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sent")
    private Date sent = new Date();

    public Interaction(User username,
                       CompanyUpload companyUpload,
                       boolean like,
                       int likeCount, int commentCount
    ) {
        this.username = username;
        this.companyUpload = companyUpload;
        this.like = like;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.comment = "";
    }

    public Interaction() {

    }

    /*
    Websocket implmentation
     */
    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    public CompanyUpload getCompanyUpload() {
        return companyUpload;
    }

    public void setCompanyUpload(CompanyUpload companyUpload) {
        this.companyUpload = companyUpload;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public User getUsername() {
        return username;
    }

    public void setUsername(User username) {
        this.username = username;
    }

    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }

    public List<String> getCommentList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(comment, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

    }

    public void setCommentList(List<String> commentList) throws JsonProcessingException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.comment = mapper.writeValueAsString(commentList);

        } catch (Exception e) {
            e.printStackTrace();
            this.comment = "";
        }
    }
    /*
    EITHER ONE ABOVE OR BELOW
     */
    //    public List<String> getComment() {
//        return comment;
//    }
//
//    public void setComment(List<String> comment) {
//        this.comment = comment;
//    }

}
