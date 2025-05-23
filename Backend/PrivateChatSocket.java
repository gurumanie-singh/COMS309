package onetoone.Messages;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import onetoone.Config.AIContentModeration;
import onetoone.Users.User;
import onetoone.Users.UserDTO;
import onetoone.Users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

@Controller
@ServerEndpoint(value = "/privateChat/{sender}/{receiver}")
public class PrivateChatSocket {

    private static PrivateMessageRepository privateMsgRepo; // Repository for private messages
    private static UserRepository userRepo; // Repository for users
    private static Map<Session, String> sessionUserMap = new Hashtable<>(); // Map of session to user key
    private static Map<String, Session> userSessionMap = new Hashtable<>(); // Map of user key to session
    private final Logger logger = LoggerFactory.getLogger(PrivateChatSocket.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public void setPrivateMessageRepository(PrivateMessageRepository repo) {
        privateMsgRepo = repo; // Inject the private message repository
    }

    @Autowired
    public void setUserRepository(UserRepository repo) {
        userRepo = repo; // Inject UserRepository
    }

    // Helper method to create UserDTO from User
    private UserDTO createUserDTO(User user) {
        Set<String> friends = user.getFriends().stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());
        return new UserDTO(user.getId(), user.getUsername(), friends);
    }

    @MessageMapping("/private-message")
    @SendTo("/topic/private-messages")
    public String handlePrivateMessage(String message, String senderUsername, String recipientUsername) {
        // Retrieve sender and recipient as UserDTO
        User sender = userRepository.findByUsername(senderUsername);
        User recipient = userRepository.findByUsername(recipientUsername);

        if (sender == null || recipient == null) {
            return "Invalid username(s). Please ensure both sender and recipient exist.";
        }

        UserDTO senderDTO = createUserDTO(sender);
        UserDTO recipientDTO = createUserDTO(recipient);

        // Validate friendship
        if (!senderDTO.getFriends().contains(recipientDTO.getUsername())) {
            return "You can only message your friends.";
        }

        // Handle sending the message
        return String.format("%s to %s: %s", senderDTO.getUsername(), recipientDTO.getUsername(), message);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("sender") String sender, @PathParam("receiver") String receiver) throws IOException {
        User senderUser = userRepo.findByUsername(sender);
        User receiverUser = userRepo.findByUsername(receiver);

        if (senderUser == null || receiverUser == null) {
            String invalidUser = (senderUser == null) ? sender : receiver;
            logger.warn("Attempt to connect with an invalid username: " + invalidUser);
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid username: " + invalidUser));
            return;
        }

        UserDTO senderDTO = createUserDTO(senderUser);
        UserDTO receiverDTO = createUserDTO(receiverUser);

        if (!senderDTO.getFriends().contains(receiverDTO.getUsername())) {
            logger.warn("Connection attempt without friendship: " + sender + " and " + receiver);
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "You can only chat with your friends."));
            return;
        }

        // Store the session with sender and receiver keys
        String userKey = sender + ":" + receiver;
        sessionUserMap.put(session, userKey);
        userSessionMap.put(userKey, session);

        // Retrieve and send chat history for both directions
        List<PrivateMessage> messages = privateMsgRepo.findBySenderAndReceiver(senderUser, receiverUser);
        messages.addAll(privateMsgRepo.findBySenderAndReceiver(receiverUser, senderUser));

        for (PrivateMessage msg : messages) {
            session.getBasicRemote().sendText(msg.getSender().getUsername() + ": " + msg.getMessageContent());
        }

        logger.info("User: " + sender + " joined private chat with " + receiver);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        String userKey = sessionUserMap.get(session);
        String[] parts = userKey.split(":");
        String sender = parts[0];
        String receiver = parts[1];

        logger.info("Message from " + sender + " to " + receiver + ": " + message);

        // Step 1: Check if the message is appropriate
        boolean isAppropriate = AIContentModeration.isMessageAppropriate(message);
        if (!isAppropriate) {
            logger.warn("Inappropriate message from " + sender + ": " + message);
            session.getBasicRemote().sendText("Your message was flagged as inappropriate and not sent.");
            return; // Block the message
        }

        User senderUser = userRepo.findByUsername(sender);
        User receiverUser = userRepo.findByUsername(receiver);

        // Step 2: Save the message to the repository
        PrivateMessage privateMessage = privateMsgRepo.save(new PrivateMessage(senderUser, message, receiverUser));

        // Step 3: Send the message to the receiver
        sendMessageToUser(receiver, sender + ": " + message);
    }


    @OnClose
    public void onClose(Session session) {
        String userKey = sessionUserMap.remove(session);
        if (userKey != null) {
            userSessionMap.remove(userKey);
            String[] parts = userKey.split(":");
            String sender = parts[0];
            String receiver = parts[1];
            logger.info(sender + " disconnected from private chat with " + receiver);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("Error in PrivateChatSocket connection", throwable);
    }

    private void sendMessageToUser(String username, String message) {
        for (Map.Entry<Session, String> entry : sessionUserMap.entrySet()) {
            String userKey = entry.getValue();
            Session userSession = entry.getKey();

            // Check if the session is associated with the username
            if (userKey.startsWith(username + ":") || userKey.endsWith(":" + username)) {
                try {
                    userSession.getBasicRemote().sendText(message);
                    logger.info("Delivered message to " + username + ": " + message);
                } catch (IOException e) {
                    logger.error("Failed to deliver message to " + username, e);
                }
            }
        }
    }
}
