package onetoone.Config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class AIContentModeration {

    private static final String HUGGINGFACE_API_URL = "https://api-inference.huggingface.co/models/unitary/toxic-bert"; // Using Toxic BERT model
    private static final String HUGGINGFACE_API_KEY = "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"; // Replace with your Hugging Face API key
    private static final Logger logger = LoggerFactory.getLogger(AIContentModeration.class);

    // Define a score threshold (e.g., flag if confidence is above 0.7)
    private static final double TOXIC_THRESHOLD = 0.7;

    public static boolean isMessageAppropriate(String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> request = new HashMap<>();
            request.put("inputs", message); // Ensure 'inputs' key is correct for the Hugging Face API.

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + HUGGINGFACE_API_KEY); // Use the new Hugging Face API key
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Wrap request and headers into an HttpEntity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            // Make the POST request
            ResponseEntity<String> response = restTemplate.postForEntity(HUGGINGFACE_API_URL, entity, String.class);

            // Log the full response for debugging
            logger.info("Hugging Face response: " + response.getBody());

            // Parse the response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());

            // Log the parsed response (check what the moderation model returns)
            logger.info("Parsed Response: " + root.toString());

            // Handle the fact that the response is a list of arrays
            if (root.isArray() && root.size() > 0 && root.get(0).isArray()) {
                JsonNode results = root.get(0);  // Get the inner array (first item in outer array)
                for (JsonNode result : results) {
                    String label = result.path("label").asText().trim(); // Ensure no leading/trailing spaces
                    double score = result.path("score").asDouble();

                    // Log the label and score for debugging
                    logger.info("Detected label: '" + label + "' with score: " + score);

                    // Check if the label is toxic and the score exceeds the threshold
                    if (!label.isEmpty() && score >= TOXIC_THRESHOLD) {
                        logger.warn("Message flagged as " + label + ": " + score);
                        return false; // Message is inappropriate
                    }
                }
            } else {
                logger.error("Unexpected response structure: " + root.toString());
                return false; // If the response is not structured as expected, treat as inappropriate
            }

            // If no toxic content was flagged, return true (appropriate)
            return true;

        } catch (Exception e) {
            logger.error("Error during content moderation", e);
            return false; // Treat errors as inappropriate content
        }
    }
}
