package onetoone.HomePage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

//import onetoone.Company.Company;
//import onetoone.Messages.ChatSocket;
//import onetoone.Messages.Message;
//import onetoone.Messages.MessageRepository;
//import onetoone.Users.User;
//import onetoone.Users.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@ServerEndpoint(value = "/view/{companyName}")
public class InteractionSocket {

    private static CompanyUploadRepository compUploadRepo;

    private static InteractionRepository interactionRepo;


    @Autowired
    public void setMessageRepository(CompanyUploadRepository repo) {
        compUploadRepo = repo;  // we are setting the static variable
    }

    @Autowired
    public void setCompanyRepository(InteractionRepository repo) {
        interactionRepo = repo;
    }

    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();
    private static List<JSONObject> posts = new ArrayList<>(); // To store post metadata
    private final Logger logger = LoggerFactory.getLogger(CompanyUploadSocket.class);


    @OnOpen
    public void onOpenUser(Session session, @PathParam("companyName") String userName) throws IOException {

        if (userName == null || userName.isEmpty()) {
            logger.error("Username is null or empty");
            session.close();
            return;
        }
        logger.info("onOpenUser: " + userName);
        sessionUsernameMap.put(session, userName);
        usernameSessionMap.put(userName, session);
        logger.info("[onOpenUser] sessionUsernameMap: {}", sessionUsernameMap);
        logger.info("[onOpenUser] sessionUsernameMap: {}", usernameSessionMap);

        logger.info("current User : {}", userName);
        CompanyUpload company = compUploadRepo.findByPostId(userName);
        if (company == null) {
            logger.warn("[OnOpen] Company not found for user: {}", userName);
            session.close();
            return;
        }

        List<CompanyUpload> uploads = compUploadRepo.findAll();
        JSONArray uploadsArray = new JSONArray();
        for (CompanyUpload upload : uploads) {
            JSONObject uploadObj = new JSONObject();
            uploadObj.put("postId", upload.getId());
            uploadObj.put("postTitle", upload.getPostTitle());
            uploadObj.put("postDescription", upload.getDescription());
            uploadObj.put("imageData", upload.getImageData());
            uploadObj.put("companyName", upload.getUploadCompanyName());
            uploadObj.put("isLiked", upload.isLiked());
            uploadsArray.put(uploadObj);
        }
        session.getBasicRemote().sendText(uploadsArray.toString());
    }

    @OnClose
    public void onCloseUserInteraction(Session session) throws IOException {
        String username = sessionUsernameMap.get(session);
        logger.info("[onClose] Closing session for user: {}", username);
        if (username == null) {
            logger.warn("[onClose] User not found for user: {}", session.getId());
        } else {
            logger.info("[onClose] Closing session for user: {}", username);
            usernameSessionMap.remove(username);
        }
        sessionUsernameMap.remove(session);
        try {
            session.close(); // Explicitly close the session if it’s not already closed.
        } catch (IOException e) {
            logger.error("[onClose] Error when closing session for {}: {}", username, e.getMessage());
            e.printStackTrace();
        }


//        usernameSessionMap.remove(username);
    }

    @OnError
    public void onErrorUserInteraction(Session session, Throwable throwable) {
        String username = sessionUsernameMap.get(session);
        logger.error("[onError] Error in session for {}: {}", username, throwable.getMessage());
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessageUser(Session session, String message) throws IOException {
        String username = sessionUsernameMap.get(session);
        logger.info("[onMessage] Message received from {}: {}", username, message);


        if (message.contains("imageData")) {
            logger.info("[OnMessage] ImageData field is present");
        } else {
            logger.info("[OnMessage] Missing imageData, field is not present");
        }
        JSONObject messageObj = new JSONObject(message);

        if (messageObj.has("action")) {
            String action = messageObj.getString("action");
            switch (action) {
                case "likeUpload":
                    handleLikes(session, messageObj, username);
                    break;
                case "commentUpload":
                    handleComments(session, messageObj, username);
                    break;
                default:
                    session.getBasicRemote().sendText("Unknown action: " + action);
            }
        }

    }

    /*
    Handle likes for each user interacting with an upload
     */
    private void handleLikes(Session session, JSONObject messageObj, String username) throws IOException {

        String uploadId = messageObj.getString("postId");

        CompanyUpload upload = compUploadRepo.findByPostId(uploadId);
        if (upload == null) {
            logger.error("Upload not found for user: {}", uploadId);
            session.getBasicRemote().sendText("Error: Upload not found for postId: " + uploadId);
            return;
        }
        Interaction interaction = interactionRepo.findByCompanyUpload(upload);
        if (interaction == null) {
            logger.error("Interaction not found for user: {}", uploadId);
            session.getBasicRemote().sendText("Error: Interaction not found for user: " + uploadId);
            return;
        }
//        Interaction compName = interactionRepo.findByUsername(username);

        interaction.setLike(true);
        interaction.setLikeCount(interaction.getLikeCount() + 1);
        interactionRepo.save(interaction);

        JSONObject response = new JSONObject();
        response.put("action", "likeUpdated");
        response.put("postId", uploadId);
        response.put("likeCount", interaction.getLikeCount());
        response.put("liked", interaction.isLike()); // may not need
        //find out how to access session properly
        session.getBasicRemote().sendText(response.toString());
        broadcast(response);
    }

    /*
    Handle comments for each user commenting on uploads
     */
    private void handleComments(Session session, JSONObject messageObj, String username) throws IOException {

        String uploadId = messageObj.getString("postId");
        String comment = messageObj.getString("comment");

        CompanyUpload upload = compUploadRepo.findByPostId(uploadId);
        if (upload == null) {
            logger.error("Upload not found for user: {}", uploadId);
            session.getBasicRemote().sendText("Error: Upload not found for postId: " + uploadId);
            return;
        }
        Interaction interaction = interactionRepo.findByCompanyUpload(upload);
        if (interaction == null) {
            logger.error("Interaction not found for user: {}", uploadId);
            session.getBasicRemote().sendText("Error: Interaction not found for user: " + uploadId);
            return;
        }

        List<String> comments = interaction.getCommentList();
        comments.add(comment);
        interaction.setCommentList(comments);
        interaction.setCommentCount(interaction.getCommentCount() + 1);
        interactionRepo.save(interaction);

        JSONObject response = new JSONObject();
        response.put("action", "commentUpdated");
        response.put("postId", uploadId);
        response.put("comments", comments);
        response.put("commentCount", interaction.getCommentCount());
        session.getBasicRemote().sendText(response.toString());
        broadcast(response);
    }

    //call to broadcast interactions
    private void broadcast(JSONObject message) {
        sessionUsernameMap.forEach((session, username) -> {
            try {
                session.getBasicRemote().sendText(message.toString());
                logger.info("[broadcast] Sent message to user: {}", username);
            } catch (IOException e) {
                logger.error("[broadcast] Error sending message to {}: {}", username, e.getMessage());
                e.printStackTrace();
            }
        });
    }

}