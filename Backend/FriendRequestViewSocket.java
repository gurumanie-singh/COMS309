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
import java.util.Set;

@Controller
@ServerEndpoint(value = "/friendRequests/{user}/{action}")
public class FriendRequestViewSocket {

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

    // Combined endpoint for viewing friend requests and checking friends
    @OnOpen
    public void onOpen(Session session, @PathParam("user") String user, @PathParam("action") String action) throws IOException {
        User activeUser = userRepo.findByUsername(user);

        if (activeUser == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid user: " + user));
            return;
        }

        // Map session to user
        sessionUserMap.put(session, user);
        userSessionMap.put(user, session);

        // Perform the action based on the WebSocket path parameter "action"
        switch (action) {
            case "viewFriendRequests":
                viewFriendRequests(session, activeUser);
                break;
            case "checkFriends":
                checkFriends(session, activeUser);
                break;
            default:
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Unknown action: " + action));
                break;
        }
    }

    // View pending friend requests logic
    private void viewFriendRequests(Session session, User user) throws IOException {
        List<FriendRequest> pendingRequests = friendRequestRepo.findByReceiverAndStatus(user, FriendRequestStatus.PENDING);

        if (pendingRequests.isEmpty()) {
            session.getBasicRemote().sendText("No pending friend requests.");
        } else {
            for (FriendRequest request : pendingRequests) {
                String username = request.getSender().getUsername();
                session.getBasicRemote().sendText(username);
            }
        }
    }


    // Check friends of a user logic
// Check friends of a user logic
    private void checkFriends(Session session, User user) throws IOException {
        Set<User> friends = user.getFriends();

        if (friends.isEmpty()) {
            session.getBasicRemote().sendText("You have no friends yet.");
        } else {
            for (User friend : friends) {
                String username = friend.getUsername();
                session.getBasicRemote().sendText(username);
            }
        }
    }


    // Notify user about the friend request status change (can be reused from the previous code)
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
