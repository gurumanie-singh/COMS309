package onetoone.Messages;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import onetoone.CareerClusters.CareerClusters;

@Entity
@Table(name = "messages")
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String username;

    @Lob
    private String message;

    private String clusterName;
    private Long timestamp; // Optionally, to track when the message was sent


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sent")
    private Date sent = new Date();

    //Constructors
    public Message() {}

    public Message(String username, String message) {
        this.username = username;
        this.message = message;
    }

    public Message(String username, String message, String clusterName) {
        this.username = username;
        this.message = message;
        this.clusterName = clusterName;
        this.timestamp = System.currentTimeMillis(); // Store the current time as timestamp
    }


    // Getters and Setters
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
    public String getMessage() {return message;}
    public void setMessage(String message) {this.message = message;}
    public Date getSent() {return sent;}
    public void setSent(Date sent) {this.sent = sent;}

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

}
