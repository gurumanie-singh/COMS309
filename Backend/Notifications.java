package onetoone.Notifications;

import jakarta.persistence.*;
import onetoone.Company.Company;
import onetoone.HomePage.CompanyUpload;
import onetoone.Users.User;

import java.util.Date;

@Table(name = "notifications")
@Entity
public class Notifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String notifyAuthor;
    private String notifyMessage;
    private String notifyType; //User, Company, type of notification
    private boolean visibility;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date timestamp = new Date();

    /*
    ADD RELATIONSHIPS
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "company_name", nullable = true)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "company_upload_id", nullable = true)
    private CompanyUpload companyUpload;

    public Notifications(String notifyAuthor, String notifyType, String notifyMessage, boolean visibility) {

        this.notifyAuthor = notifyAuthor;
        this.notifyType = notifyType;
        this.notifyMessage = notifyMessage;
        this.visibility = visibility;
    }

    public Notifications() {

    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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

    public String getNotifyAuthor() {
        return notifyAuthor;
    }

    public void setNotifyAuthor(String notifyAuthor) {
        this.notifyAuthor = notifyAuthor;
    }

    public String getNotifyType() {

        return notifyType;
    }

    public void setNotifyType(String notifyType) {
        this.notifyType = notifyType;
    }


    public String getNotifyMessage() {
        return notifyMessage;
    }

    public void setNotifyMessage(String notifyMessage) {
        this.notifyMessage = notifyMessage;
    }


}
