package onetoone.Messages;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import onetoone.CareerClusters.CareerClusters;
import onetoone.CareerClusters.CareerClustersRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@ServerEndpoint(value = "/chat/{clusterName}/{username}")
public class ClusterChatSocket {

    private static MessageRepository msgRepo;
    private static CareerClustersRepository careerClustersRepo; // New repository for career clusters
    private static UserRepository userRepo; // New repository for users
    private static Map<Session, String> sessionClusterMap = new Hashtable<>();
    private static Map<String, Session> clusterSessionMap = new Hashtable<>();
    private final Logger logger = LoggerFactory.getLogger(ClusterChatSocket.class);

    @Autowired
    public void setMessageRepository(MessageRepository repo) {
        msgRepo = repo;
    }

    @Autowired
    public void setCareerClustersRepository(CareerClustersRepository repo) {
        careerClustersRepo = repo; // Inject the career clusters repository
    }

    @Autowired
    public void setUserRepository(UserRepository repo) { // Inject UserRepository
        userRepo = repo;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("clusterName") String clusterName, @PathParam("username") String username) throws IOException {
        // Validate the cluster name
        CareerClusters cluster = careerClustersRepo.findByClusterName(clusterName);
        if (cluster == null) {
            logger.warn("Attempt to connect to an invalid cluster: " + clusterName);
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid cluster name: " + clusterName));
            return;
        }

        // Validate the username
        User user = userRepo.findByUsername(username);
        if (user == null) {
            logger.warn("Attempt to connect with an invalid username: " + username);
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid username: " + username));
            return;
        }

        // Proceed to connect since the username and cluster name are valid
        String userClusterKey = clusterName + ":" + username;
        sessionClusterMap.put(session, userClusterKey);
        clusterSessionMap.put(userClusterKey, session);

        // Retrieve chat history for the cluster
        List<Message> messages = msgRepo.findByClusterName(clusterName);
        for (Message msg : messages) {
            session.getBasicRemote().sendText(msg.getUsername() + ": " + msg.getMessage());
        }

        broadcast(clusterName, "User: " + username + " joined " + clusterName + " chat");
    }



    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        String userClusterKey = sessionClusterMap.get(session);
        String[] parts = userClusterKey.split(":");
        String clusterName = parts[0];
        String username = parts[1];

        // Handle direct messages
        if (message.startsWith("@")) {
            String destUsername = message.split(" ")[0].substring(1);
            String destKey = clusterName + ":" + destUsername;
            sendMessageToUser(destKey, "[DM] " + username + ": " + message);
            sendMessageToUser(userClusterKey, "[DM] " + username + ": " + message);
        } else {
            broadcast(clusterName, username + ": " + message);
        }

        // Save the message with clusterName
        msgRepo.save(new Message(username, message, clusterName));
    }


    @OnClose
    public void onClose(Session session) throws IOException {
        String userClusterKey = sessionClusterMap.remove(session);
        String[] parts = userClusterKey.split(":");
        String clusterName = parts[0];
        String username = parts[1];
        clusterSessionMap.remove(userClusterKey);
        broadcast(clusterName, username + " disconnected from " + clusterName + " chat");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("Error in ClusterChatSocket connection", throwable);
    }

    private void sendMessageToUser(String userClusterKey, String message) {
        try {
            clusterSessionMap.get(userClusterKey).getBasicRemote().sendText(message);
        } catch (IOException e) {
            logger.error("Exception while sending message", e);
        }
    }

    private void broadcast(String clusterName, String message) {
        sessionClusterMap.forEach((session, userClusterKey) -> {
            if (userClusterKey.startsWith(clusterName + ":")) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    logger.error("Exception in broadcast", e);
                }
            }
        });
    }
}
