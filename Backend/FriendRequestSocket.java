package onetoone.FriendRequests;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Controller
@ServerEndpoint(value = "/friendRequests/{user}/{action}/{targetUser}")
public class FriendRequestSocket {

    private static FriendRequestRepository friendRequestRepo;
    private static UserRepository userRepo;
    private static Map<Session, String> sessionUserMap = new Hashtable<>();
    private static Map<String, Session> userSessionMap = new Hashtable<>();
    private final Logger logger = LoggerFactory.getLogger(FriendRequestSocket.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public void setFriendRequestRepository(FriendRequestRepository repo) {
        friendRequestRepo = repo;
    }

    @Autowired
    public void setUserRepository(UserRepository repo) {
        userRepo = repo;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("user") String user, @PathParam("action") String action, @PathParam("targetUser") String targetUser) throws IOException {
        User activeUser = userRepo.findByUsername(user);
        User targetUserObj = userRepo.findByUsername(targetUser);

        if (activeUser == null || targetUserObj == null) {
            String invalidUser = (activeUser == null) ? user : targetUser;
            logger.warn("Attempt to connect with an invalid username: " + invalidUser);
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid username: " + invalidUser));
            return;
        }

        // Map session to user
        sessionUserMap.put(session, user);
        userSessionMap.put(user, session);

        // Perform the action based on the WebSocket path parameter "action"
        switch (action) {
            case "sendFriendRequest":
                sendFriendRequest(session, activeUser, targetUserObj);
                break;
            case "acceptFriendRequest":
                acceptFriendRequest(session, targetUserObj, activeUser);
                break;
            case "rejectFriendRequest":
                rejectFriendRequest(session, targetUserObj, activeUser);
                break;
            default:
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Unknown action: " + action));
                break;
        }
    }

    // Send friend request logic
    private void sendFriendRequest(Session session, User sender, User receiver) throws IOException {
        // Check if there's already a pending friend request
        sender = userRepo.findByUsername(sender.getUsername());
        receiver = userRepo.findByUsername(receiver.getUsername());

        // Check if they are already friends
        if (sender.getFriends().contains(receiver)) {
            session.getBasicRemote().sendText("You are already friends with " + receiver.getUsername());
            return;
        }

        FriendRequest existingRequest = friendRequestRepo.findByReceiverAndSenderAndStatus(receiver, sender, FriendRequestStatus.PENDING);
        if (existingRequest != null) {
            session.getBasicRemote().sendText("There is already a pending friend request between you and " + receiver.getUsername());
            return;
        }

        // Otherwise, create a new friend request
        FriendRequest request = new FriendRequest(sender, receiver, FriendRequestStatus.PENDING);
        friendRequestRepo.save(request);
        notifyUser(receiver, "You have received a friend request from " + sender.getUsername());
    }

    // Accept friend request logic
    private void acceptFriendRequest(Session session, User sender, User receiver) throws IOException {
        FriendRequest request = friendRequestRepo.findByReceiverAndSenderAndStatus(receiver, sender, FriendRequestStatus.PENDING);

        if (request != null) {
            request.setStatus(FriendRequestStatus.ACCEPTED);
            friendRequestRepo.save(request);

            // Add both users as friends
            sender.addFriend(receiver);
            receiver.addFriend(sender);

            // Save the updated users to the database
            userRepo.save(sender);  // Save sender after adding receiver as a friend
            userRepo.save(receiver); // Save receiver after adding sender as a friend

            notifyUser(sender, receiver.getUsername() + " has accepted your friend request.");
            notifyUser(receiver, "You have accepted " + sender.getUsername() + "'s friend request.");
        } else {
            session.getBasicRemote().sendText("No pending friend request from " + sender.getUsername());
        }
    }

    // Reject friend request logic
    private void rejectFriendRequest(Session session, User sender, User receiver) throws IOException {
        FriendRequest request = friendRequestRepo.findByReceiverAndSenderAndStatus(receiver, sender, FriendRequestStatus.PENDING);

        if (request != null) {
            request.setStatus(FriendRequestStatus.REJECTED);
            friendRequestRepo.save(request);

            notifyUser(sender, receiver.getUsername() + " has rejected your friend request.");
            notifyUser(receiver, "You have rejected " + sender.getUsername() + "'s friend request.");
        } else {
            session.getBasicRemote().sendText("No pending friend request to reject.");
        }
    }

//    // View friend requests logic
//    private void viewFriendRequests(Session session, User user) throws IOException {
//        StringBuilder response = new StringBuilder("Pending friend requests:\n");
//        List<FriendRequest> pendingRequests = friendRequestRepo.findByReceiverAndStatus(user, FriendRequestStatus.PENDING);
//
//        if (pendingRequests.isEmpty()) {
//            response.append("No pending friend requests.\n");
//        } else {
//            for (FriendRequest request : pendingRequests) {
//                response.append("From: ").append(request.getSender().getUsername()).append("\n");
//            }
//        }
//
//        session.getBasicRemote().sendText(response.toString());
//    }
//
//    // Check if the two users are friends
//    private void checkIfFriends(Session session, User user1, User user2) throws IOException {
//        // Reload the users from the database to ensure we're checking the latest state
//        user1 = userRepo.findByUsername(user1.getUsername());
//        user2 = userRepo.findByUsername(user2.getUsername());
//
//        // Check if the users are friends by querying the database
//        boolean areFriends = user1.getFriends().contains(user2) && user2.getFriends().contains(user1);
//
//        // Construct the message based on whether they are friends
//        String message = areFriends
//                ? "You and " + user2.getUsername() + " are friends."
//                : "You and " + user2.getUsername() + " are not friends.";
//
//        session.getBasicRemote().sendText(message);
//    }

    // Notify user about the friend request status change
    private void notifyUser(User user, String message) {
        for (Map.Entry<Session, String> entry : sessionUserMap.entrySet()) {
            String userKey = entry.getValue();
            Session userSession = entry.getKey();
            if (userKey.startsWith(user.getUsername())) {
                try {
                    userSession.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    logger.error("Failed to notify " + user.getUsername(), e);
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessionUserMap.remove(session);
        logger.info("User disconnected.");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("Error in FriendRequestSocket connection", throwable);
    }
}
