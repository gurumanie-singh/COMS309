package onetoone.Notifications;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import onetoone.HomePage.CompanyUploadRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;

@Controller
@ServerEndpoint(value = "/notifications/{username}")
public class NotificationsSocket {

    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();
    private static List<JSONObject> notifications = new ArrayList<>(); // To store notifications
    private final Logger logger = LoggerFactory.getLogger(NotificationsSocket.class);

    private static NotificationsRepository notificationsRepository;

    private static UserRepository userRepo;

    private static CompanyUploadRepository companyUploadRepo;

    @Autowired
    public void setNotificationsRepository(NotificationsRepository repository) {
        notificationsRepository = repository;
    }

    @Autowired
    public void setCompanyUploadRepository(CompanyUploadRepository repository) {
        companyUploadRepo = repository;
    }

    @Autowired
    public void setUserRepo(UserRepository repository) {
        userRepo = repository;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException {

        if (username == null || username.isEmpty()) {
            logger.error("Username is null or empty");
            session.close();
            return;
        }

        logger.info("[onOpen] Session opened for user: {}", username);
        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);

        // Send existing notifications to the newly connected user
        User user = userRepo.findByUsername(username);
        if (user == null) {
            logger.info("[onOpen] User not found for username: {}", username);
            session.close();
            return;
        }
//        List<String>
        sendExistingNotifications(session, user);
    }

    private void sendExistingNotifications(Session session, User user) throws IOException {
        //test cases
//        initializeDemoNotifications();
        logger.info("[sendExistingNotifications] Sending existing notifications to session ID: {}", session.getId());
        List<Notifications> userNotifications = notificationsRepository.findAll();
        JSONArray notificationsArray = new JSONArray();
        if (userNotifications.isEmpty()) {
            logger.info("[sendExistingNotifications] User not found for username: {}", user.getUsername());
            session.getBasicRemote().sendText("[]");
            return;
        } else {
            userNotifications.forEach(notification -> {
                JSONObject notificationJson = new JSONObject();
                notificationJson.put("id", notification.getId());
                notificationJson.put("notifyType", notification.getNotifyType());
                notificationJson.put("notifyMessage", notification.getNotifyMessage());
                notificationJson.put("notifyAuthor", notification.getNotifyAuthor());
                notificationJson.put("visibility", notification.isVisibility());
                notificationsArray.put(notificationJson);
            });
        }
//        try {
//            JSONArray existingNotificationsArray = new JSONArray(notifications);
            session.getBasicRemote().sendText(notificationsArray.toString());
            logger.info("[sendExistingNotifications] Sent {} notifications to session ID: {}", notificationsArray.length(), session.getId());
//        } catch (IOException e) {
//            logger.error("[sendExistingNotifications] Error sending notifications: {}", e.getMessage());
//            e.printStackTrace();
//            session.close();
//        }
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        String username = sessionUsernameMap.get(session);
        logger.info("[onMessage] Message received from {}: {}", username, message);

        if ("clearNotifications".equals(message)) {
            // Handle clearing notifications (setting visibility to false)
            clearNotifications(username);
        } else {
            logger.warn("[onMessage] Received unknown message: {}", message);
        }
    }

    private void clearNotifications(String username) {
        logger.info("[clearNotifications] Clearing all notifications (setting visibility to false).");
        User user = userRepo.findByUsername(username);

        // Set visibility to false for all notifications
        for (JSONObject notification : notifications) {
            notification.put("visibility", true);
        }
        notificationsRepository.deleteByUser(user);
        // Broadcast the updated notifications (with visibility set to false)
        broadcastUpdatedNotifications();
    }

    private void broadcastUpdatedNotifications() {
        JSONArray updatedNotificationsArray = new JSONArray(notifications);
        logger.info("[broadcastUpdatedNotifications] Broadcasting updated notifications to all users: {}", updatedNotificationsArray.length());

        sessionUsernameMap.forEach((session, username) -> {
            try {
                session.getBasicRemote().sendText(updatedNotificationsArray.toString());
                logger.info("[broadcastUpdatedNotifications] Sent updated notifications to user: {}", username);
            } catch (IOException e) {
                logger.error("[broadcastUpdatedNotifications] Error sending notifications to {}: {}", username, e.getMessage());
            }
        });
    }

    private JSONObject handleNotificationMessage(String username, JSONObject jsonMessage) {
        try {
            // Confirm required fields, including visibility
            if (!jsonMessage.has("notifyType") || !jsonMessage.has("notifyAuthor")
                    || !jsonMessage.has("notifyMessage") || !jsonMessage.has("visibility")) {
                logger.warn("[handleNotificationMessage] Missing fields in notification from {}", username);
                return null;
            }

            // Create the notification object
            JSONObject notification = new JSONObject();
            notification.put("notifyType", jsonMessage.getString("notifyType"));
            notification.put("notifyAuthor", jsonMessage.getString("notifyAuthor"));
            notification.put("notifyMessage", jsonMessage.getString("notifyMessage"));
            notification.put("visibility", jsonMessage.getBoolean("visibility")); // Store visibility

            // Store notification
            notifications.add(notification);
            logger.info("[handleNotificationMessage] Notification created and stored for user: {}", username);

            return notification;
        } catch (Exception e) {
            logger.error("[handleNotificationMessage] Error creating notification from {}: {}", username, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    @OnClose
    public void onClose(Session session) {
        String username = sessionUsernameMap.get(session);
        logger.info("[onClose] Closing session for user: {}", username);

        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        String username = sessionUsernameMap.get(session);
        logger.error("[onError] Error in session for {}: {}", username, throwable.getMessage());
        throwable.printStackTrace();
    }

    private void broadcast(String message) {
        sessionUsernameMap.forEach((session, username) -> {
            try {
                session.getBasicRemote().sendText(message);
                logger.info("[broadcast] Sent notification to user: {}", username);
            } catch (IOException e) {
                logger.error("[broadcast] Error sending message to {}: {}", username, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void notifyNewPost(String author, String message) {

        JSONObject notification = new JSONObject();
        notification.put("notifyType", "New Post");
        notification.put("notifyAuthor", author);
        notification.put("notifyMessage", message);
        notification.put("visibility", true); // Default visibility as true for new posts

        // Add the notification to the stored list
        notifications.add(notification);
        // Broadcast the notification to all connected users
        broadcast(notification.toString());
    }


    private void initializeDemoNotifications() {

        if (notifications.isEmpty()) {
            JSONObject notify1 = new JSONObject();
            notify1.put("notifyType", "New Message");
            notify1.put("notifyAuthor", "Jonathan");
            notify1.put("notifyMessage", "Hey man! How's it going?");
            notify1.put("visibility", true);
            notifications.add(notify1);

            JSONObject notify2 = new JSONObject();
            notify2.put("notifyType", "New Post");
            notify2.put("notifyAuthor", "Johnson & Johnson");
            notify2.put("notifyMessage", "New internship opportunities for Summer 2024!");
            notify2.put("visibility", true);
            notifications.add(notify2);

            JSONObject notify3 = new JSONObject();
            notify3.put("notifyType", "New Message");
            notify3.put("notifyAuthor", "Jane");
            notify3.put("notifyMessage", "You interested in a company I just found?");
            notify3.put("visibility", true); // Set visibility to false for demo purposes
            notifications.add(notify3);

            JSONObject notify4 = new JSONObject();
            notify4.put("notifyType", "Post Update");
            notify4.put("notifyAuthor", "Intel");
            notify4.put("notifyMessage", "Update: All our internship opportunities have filled up! See you next Summer!");
            notify4.put("visibility", true);
            notifications.add(notify4);

            JSONObject notify5 = new JSONObject();
            notify5.put("notifyType", "New Post");
            notify5.put("notifyAuthor", "Google");
            notify5.put("notifyMessage", "We're hosting a new Hackathon! Join us this coming Fall!");
            notify5.put("visibility", true);
            notifications.add(notify5);

            JSONObject notify6 = new JSONObject();
            notify6.put("notifyType", "New Message");
            notify6.put("notifyAuthor", "Joshua");
            notify6.put("notifyMessage", "Do you want to join this new community? I think it fits our major better.");
            notify6.put("visibility", true);
            notifications.add(notify6);

            // Adding new notifications with 'New Follow Request'
            JSONObject notify7 = new JSONObject();
            notify7.put("notifyType", "New Follow Request");
            notify7.put("notifyAuthor", "Alice");
            notify7.put("notifyMessage", "Alice wants to follow you!");
            notify7.put("visibility", true);
            notifications.add(notify7);

            JSONObject notify8 = new JSONObject();
            notify8.put("notifyType", "New Follow Request");
            notify8.put("notifyAuthor", "Bob");
            notify8.put("notifyMessage", "Bob wants to follow you! Check out his profile.");
            notify8.put("visibility", true);
            notifications.add(notify8);

            JSONObject notify9 = new JSONObject();
            notify9.put("notifyType", "New Follow Request");
            notify9.put("notifyAuthor", "Charlie");
            notify9.put("notifyMessage", "Charlie has sent you a follow request.");
            notify9.put("visibility", false); // Setting visibility to false for demo purposes
            notifications.add(notify9);

            // Adding additional notifications of other types
            JSONObject notify10 = new JSONObject();
            notify10.put("notifyType", "New Post");
            notify10.put("notifyAuthor", "Microsoft");
            notify10.put("notifyMessage", "New job openings at Microsoft! Check them out.");
            notify10.put("visibility", true);
            notifications.add(notify10);

            JSONObject notify11 = new JSONObject();
            notify11.put("notifyType", "Post Update");
            notify11.put("notifyAuthor", "Amazon");
            notify11.put("notifyMessage", "Amazon internship opportunities updated. New roles available!");
            notify11.put("visibility", true);
            notifications.add(notify11);

            JSONObject notify12 = new JSONObject();
            notify12.put("notifyType", "New Message");
            notify12.put("notifyAuthor", "Emily");
            notify12.put("notifyMessage", "Hey, want to grab lunch this weekend?");
            notify12.put("visibility", true);
            notifications.add(notify12);
        }
    }

}
