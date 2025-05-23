package onetoone.Company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.*;
import onetoone.HomePage.CompanyUpload;
import onetoone.Notifications.Notifications;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/// *
/// /@author Doyle Chism and Gurumanie Singh
/// / */

@Table(name = "company")
@Transactional
@Entity
public class Company {

    //    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private int id;
    @Id
    @Column(unique = true, nullable = false)
    private String companyName;
    private String companyPassword;
    private String companyEmail;
    private String companyPhoneNumber;
    private String countryOrigin;
    private String companyMoto;
    private String dateFounded;
    private String companyDescription;

    /*
    RELATIONSHIPS
     */
    // OneToMany relationship with CompanyUpload
    @JsonIgnore
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<CompanyUpload> uploads;

    @JsonIgnore
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notifications> notifications = new ArrayList<>();

    public Company(String companyName, String companyPassword,
                   String companyEmail, String companyPhoneNumber,
                   String countryOrigin, String companyMoto,
                   String companyDescription, String dateFounded) {
//        this.id = id;
        this.companyName = companyName;
        this.companyPassword = companyPassword;
        this.companyEmail = companyEmail;
        this.companyPhoneNumber = companyPhoneNumber;
        this.countryOrigin = countryOrigin;
        this.companyMoto = companyMoto;
        this.companyDescription = companyDescription;
        this.dateFounded = dateFounded;

    }

    public Company() {
    }


    public String getdateFounded() {
        return dateFounded;
    }

    public void setdateFounded(String dateFounded) {
        this.dateFounded = dateFounded;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public String getCompanyPhoneNumber() {
        return companyPhoneNumber;
    }

    public void setCompanyPhoneNumber(String companyPhoneNumber) {
        this.companyPhoneNumber = companyPhoneNumber;
    }

    public String getCountryOrigin() {
        return countryOrigin;
    }

    public void setCountryOrigin(String countryOrigin) {
        this.countryOrigin = countryOrigin;
    }

    public String getCompanyMoto() {
        return companyMoto;
    }

    public void setCompanyMoto(String companyMoto) {
        this.companyMoto = companyMoto;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public String getCompanyPassword() {
        return companyPassword;
    }

    public void setCompanyPassword(String companyPassword) {
        this.companyPassword = companyPassword;
    }

    public List<CompanyUpload> getUploads() {
        return uploads;
    }

    public void setUploads(List<CompanyUpload> uploads) {
        this.uploads = uploads;
    }

    public List<Notifications> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notifications> notifications) {
        this.notifications = notifications;
    }


}
