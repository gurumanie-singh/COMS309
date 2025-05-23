package onetoone.HomePage;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import onetoone.Company.Company;
import onetoone.Company.CompanyRepository;
import onetoone.Messages.ChatSocket;
import onetoone.Notifications.Notifications;
import onetoone.Notifications.NotificationsRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPOutputStream;

@Controller      // this is needed for this to be an endpoint to springboot
@ServerEndpoint(value = "/post/{companyName}")  // this is Websocket url
public class CompanyUploadSocket {

    private static CompanyUploadRepository compUploadRepo;

    private static CompanyRepository companyRepo;

    private static NotificationsRepository notificationsRepo;


    @Autowired
    public void setMessageRepository(CompanyUploadRepository repo) {
        compUploadRepo = repo;  // we are setting the static variable
    }

    @Autowired
    public void setCompanyRepository(CompanyRepository repo) {
        companyRepo = repo;
    }

    @Autowired
    public void setNotificationsRepository(NotificationsRepository repo) {
        notificationsRepo = repo;
    }

    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();
    private static List<JSONObject> posts = new ArrayList<>(); // To store post metadata
    private final Logger logger = LoggerFactory.getLogger(CompanyUploadSocket.class);

    /*
    Maybe add @PathParam
     */
    /*
    TYPE THIS INTO SQL TAB for properly storing queires
    ALTER TABLE company_upload MODIFY COLUMN image_data LONGBLOB;
     */


    @OnOpen
    public void onOpenCompanyUpload(Session session, @PathParam("companyName") String username)
            throws IOException {
        session.setMaxTextMessageBufferSize(1024 * 1024 * 20); // 20MB
        session.setMaxBinaryMessageBufferSize(1024 * 1024 * 20); // 20 MB
        logger.info("[OnOpen] session for user: {}", username);
        // store connecting user information
        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);
        // Send existing posts to the newly connected user
        logger.info("current User : {}", username);
        Company company = companyRepo.findByCompanyName(username);
        if (company == null) {
            logger.warn("[OnOpen] Company not found for user: {}", username);
            session.close();
            return;
        }

        List<CompanyUpload> uploads = compUploadRepo.findAll();
        JSONArray arr = new JSONArray();
        //find it individually
        for (CompanyUpload upload : uploads) {

            JSONObject obj = new JSONObject();
            obj.put("postId", upload.getPostID());
            obj.put("postDescription", upload.getDescription());
            obj.put("postTitle", upload.getPostTitle());
            obj.put("imageData", upload.getImageData());
            obj.put("companyName", upload.getUploadCompanyName());
            obj.put("isLiked", upload.isLiked());
            arr.put(obj);
        }
        session.getBasicRemote().sendText(arr.toString());
        //find based on all data
//        sendExistingPosts();
    }

    @OnMessage
    public void onMessageCompanyUpload(Session session, String message) throws IOException {
        String username = sessionUsernameMap.get(session);
        logger.info("[onMessage] Message received from {}: {}", username, message);
        logger.info("[onMessage] Received messageSize: {} bytes", message.length());

        if (message.contains("imageData")) {
            logger.info("[OnMessage] imageData field is present");
        } else {
            logger.warn("[OnMessage] Missing imageData field");
        }
        JSONObject jsonMessage = new JSONObject(message);
//        handlePostMessage(jsonMessage, username);
        //list descriptions
//        int id = jsonMessage.getInt("id");
        String postId = jsonMessage.getString("postId");
        String postTitle = jsonMessage.getString("postTitle");
        String postDescription = jsonMessage.getString("postDescription");
        String encodedImageData = jsonMessage.getString("imageData");
        boolean isLiked = jsonMessage.getBoolean("isLiked");
//        String companyName = jsonMessage.getString("companyName");

        Company company = companyRepo.findByCompanyName(username);
        if (company == null) {
            logger.warn("[onMessage] Company not found for user: {}", username);
            return;
        }
        CompanyUpload companyUpload = new CompanyUpload();
        companyUpload.setPostID(postId);
        companyUpload.setPostTitle(postTitle);
        companyUpload.setDescription(postDescription);
        companyUpload.setImageData(compressImage(encodedImageData));
        companyUpload.setUploadCompanyName(company);
        companyUpload.setLiked(isLiked);
        //add to database and broadcast
        logger.info("Company name {}", companyUpload.getUploadCompanyName());
        compUploadRepo.save(companyUpload);
        broadcast(jsonMessage.toString());

        //assign Notifications
        Notifications notifications = new Notifications();
        notifications.setNotifyAuthor(company.getCompanyName());
        notifications.setNotifyType("New Upload");
        notifications.setNotifyMessage("New Upload: " + postTitle + " by " + company.getCompanyName());
        notifications.setCompany(company);
        notifications.setCompanyUpload(companyUpload);
        notifications.setVisibility(true);
        notificationsRepo.save(notifications);
        //broadcast to user
        JSONObject notiObject = new JSONObject();
        notiObject.put("id", notifications.getId());
        notiObject.put("notifyType", notifications.getNotifyType());
        notiObject.put("notifyMessage", notifications.getNotifyMessage());
        notiObject.put("notifyAuthor", notifications.getNotifyAuthor());
        notiObject.put("visibility", notifications.isVisibility());
        broadcast(notiObject.toString());
    }

    @OnClose
    public void onCloseCompanyUpload(Session session) throws IOException {
        String username = sessionUsernameMap.get(session);
        logger.info("[onClose] Closing session for user: {}", username);

        try {
            session.close(); // Explicitly close the session if it’s not already closed.
        } catch (IOException e) {
            logger.error("[onClose] Error when closing session for {}: {}", username, e.getMessage());
            e.printStackTrace();
        }

        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);
    }

    @OnError
    public void onErrorCompanyUpload(Session session, Throwable throwable) {
        String username = sessionUsernameMap.get(session);
        logger.error("[onError] Error in session for {}: {}", username, throwable.getMessage());
        throwable.printStackTrace();
    }

    private void broadcast(String message) {
        sessionUsernameMap.forEach((session, username) -> {
            try {
                session.getBasicRemote().sendText(message);
                logger.info("[broadcast] Sent message to user: {}", username);
            } catch (IOException e) {
                logger.error("[broadcast] Error sending message to {}: {}", username, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public String compressImage(String imageData) throws IOException {

        byte[] imageBytes = Base64.getDecoder().decode(imageData);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(imageBytes);
        }
        return Base64.getEncoder().encodeToString(imageBytes);

    }

//    private void broadcastPosts() {
//        try {
//            JSONArray existingPostsJsonArray = new JSONArray();
//
//            // Sort posts if needed, e.g., by "postId" or other criteria
//            posts.stream()
//                    .sorted(Comparator.comparing(post -> post.getString("postId")))  // Change "postId" to desired field
//                    .forEach(existingPostsJsonArray::put);
//
//            for (Session session : sessionUsernameMap.keySet()) {
//                if (session.isOpen()) {
//                    session.getBasicRemote().sendText(existingPostsJsonArray.toString());
//                }
//            }
//            logger.info("[broadcastPosts] Broadcasted {} posts to all users.", posts.size());
//        } catch (IOException e) {
//            logger.error("[broadcastPosts] Error broadcasting posts: {}", e.getMessage());
//            e.printStackTrace();
//        }
//    }


    private void handlePostMessage(JSONObject jsonMessage, String username) throws IOException {

        try {
            // Confirm all required fields are present in the JSON message
            if (!jsonMessage.has("postId") || !jsonMessage.has("companyName") ||
                    !jsonMessage.has("postTitle") || !jsonMessage.has("postDescription")
                    || !jsonMessage.has("imageData")) {
                logger.warn("[handlePostMessage] Missing fields in post message from {}", username);
                return;
            }

            // Extract fields from the incoming post message
            String postId = jsonMessage.getString("postId");
            String companyName = jsonMessage.getString("companyName");
            String postTitle = jsonMessage.getString("postTitle");
            String postDescription = jsonMessage.getString("postDescription");
            String encodedImageData = jsonMessage.getString("imageData");

            logger.info("[handlePostMessage] Received post from {}: postId={}, companyName={} ",
                    username, postId, companyName);
            // Create and store the post
            JSONObject post = new JSONObject();
            post.put("postId", postId);
            post.put("companyName", companyName);
            post.put("postTitle", postTitle);
            post.put("postDescription", postDescription);
            post.put("imageData", encodedImageData);
            post.put("isLiked", false);
            //store data in map and save info
            posts.add(post);
            logger.info("[handlePostMessage] Successfully created and stored new post: postId={}", postId);

        } catch (Exception e) {
            logger.error("[Post Handling Exception] Error processesing upload message from {}: {}", username, e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendExistingPosts(Session session) throws IOException {
        logger.info("[sendExistingPosts] Sending existing posts to session ID: {}", session.getId());
        try {
            JSONArray existingPostsJsonArray = new JSONArray();

            for (JSONObject post : posts) {
                existingPostsJsonArray.put(post);
            }

            session.getBasicRemote().sendText(existingPostsJsonArray.toString());
            logger.info("[sendExistingPosts] Successfully sent {} posts to session ID: {}", posts.size(), session.getId());


        } catch (IOException e) {
            logger.error("[sendExistingPosts] Error sending posts to session ID {}: {}", session.getId(), e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleImageUpload(byte[] jsonImage) throws IOException {

//        String imageData = jsonImage.getB("imageData");
//        String postId = jsonMessage.getString("postId");
//        byte[] imageBytes = Base64.getDecoder().decode(imageData);
        //set filePath
        //ask kenny where to store the file with directory = "/src/path"
//        File destFile = new File( File.separator + postId);
//        Files.write(destFile.toPath(), imageBytes);
//
//       CompanyUpload image = compUploadRepo.findByPostId(postId);
//       image.setPostID(postId);
//       image.setImageData(imageBytes);
//       compUploadRepo.save(image);
//       logger.info("[handleImageUpload] Successfully uploaded image: {}", destFile.getAbsolutePath());
    }

//    private void sendImageUpload(Session session) throws IOException {
//        //sendByImageData
//        //check image type
//        String postId = (String) session.getUserProperties().get("postId");
//        if (postId == null) {
//            logger.error("[sendImageUpload] Missing user properties for postId: {}", postId);
//            return;
//        }
//        CompanyUpload image = compUploadRepo.findByPostId(postId);
//        String imageData = Base64.getEncoder().encodeToString(image.getImageData());
////        File imageFile = new File();
////        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
//        JSONObject postJson = new JSONObject();
//        postJson.put("postId", postId);
//        postJson.put("imageData", imageData);
//        session.getBasicRemote().sendText(postJson.toString());
//        logger.info("[sendImageUpload] Successfully sent {} image: {}", postId, imageData);
//    }

    private byte[] loadImageAsByteArray(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        return Files.readAllBytes(imageFile.toPath());
    }

    private int findPostIndexById(String postId) {
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getString("postId").equals(postId)) {
                return i; // Return the index if found
            }
        }
        return -1; // Not found
    }

    private JSONObject findPostById(String postId) {
        for (JSONObject post : posts) {
            if (post.getString("postId").equals(postId)) {
                return post; // Return the post if found
            }
        }
        return null; // Not found
    }


}

