package onetoone.ProfilePhoto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@Tag(name = "Image Controller", description = "REST APIs related to Image Entity")
public class ImageController {

    // replace this! careful with the operating system in use
    private static String directory = "/Users/guru/Pictures";
//    private static String directory = "/tmp/ProfilePictures";

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Get profile image by username", description = "Retrieve the profile image for a user by their username.")
    @GetMapping(value = "/images/{username}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImageByUsername(@PathVariable String username) throws IOException {
        Image image = imageRepository.findByUser_Username(username);
        if (image == null) {
            throw new RuntimeException("Image not found for username: " + username);
        }
        File imageFile = new File(image.getFilePath());
        return Files.readAllBytes(imageFile.toPath());
    }

    @Operation(summary = "Upload profile image for user", description = "Upload and save a profile image for a user by their username.")
    @PostMapping("/images/{username}")
    public String handleFileUpload(@PathVariable String username, @RequestParam("image") MultipartFile imageFile) {
        try {
            // Check if user exists
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return "User not found: " + username;
            }

            // Ensure the /tmp/ProfilePictures directory exists before saving the file
            File profilePicturesDir = new File(directory);
            if (!profilePicturesDir.exists()) {
                boolean created = profilePicturesDir.mkdirs();
                if (!created) {
                    return "Failed to create directory: " + directory;
                }
            }

            // Save the image to the /tmp/ProfilePictures directory
            File destinationFile = new File(profilePicturesDir, imageFile.getOriginalFilename());
            imageFile.transferTo(destinationFile);

            // Create or update the Image object
            Image image = user.getProfilePicture();
            if (image == null) {
                image = new Image();
                user.setProfilePicture(image);
            }
            image.setFilePath(destinationFile.getAbsolutePath());
            image.setUser(user);

            // Save the image and user with profile picture updated
            imageRepository.save(image);
            userRepository.save(user);

            return "File uploaded successfully: " + destinationFile.getAbsolutePath();
        } catch (IOException e) {
            return "Failed to upload file: " + e.getMessage();
        }
    }

}
