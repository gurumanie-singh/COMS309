package onetoone.University;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Company.Company;
import onetoone.Company.CompanyRepository;
import onetoone.Degrees.Degrees;
import onetoone.Degrees.DegreesRepository;
//import onetoone.UniversityName.UniversityNameRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

@RestController
@Tag(name = "University Controller", description = "REST APIs related to University Entity")
public class UniversityController {

    @Autowired
    UniversityRepository universityRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @Operation(summary = "Get all Universities", description = "Retrieve a list of all universities from the database")
    @GetMapping("/universities")
    public ResponseEntity<List<University>> getAllUniversities() {
        List<University> universities = universityRepository.findAll();
        return ResponseEntity.ok(universities);
    }
    @Operation(summary = "Get University by Id", description = "Retrieve a specific university by the id in the database")
    @GetMapping(path = "/universities/{id}")
    University getUniversityById(@PathVariable int id) {
        return universityRepository.findById(id);
    }

    @Operation(summary = "Get University by Prefix", description = "search for a university and it will display the names based on the prefix")
    @GetMapping("/universities/search/{prefix}")
    public ResponseEntity<List<University>> getUniversitiesByPrefix(@PathVariable String prefix) {
        List<University> universities = universityRepository.findByUniversityNameContaining((prefix));
        if (universities.isEmpty()) {
            return ResponseEntity.noContent().build(); // Return 204 if no matching universities are found
        }
        return ResponseEntity.ok(universities);
    }
    @Operation(summary = "Create new University", description = "Creates a new university and sets all the information associated with it")
    @PostMapping("/universities")
    public ResponseEntity<Object> createUniversity(@RequestBody University university) {
        System.out.println("Received university: " + university);
        if (university == null || university.getUniversityName() == null || university.getUniversityName().isEmpty()) {
            return ResponseEntity.badRequest().body("University name cannot be null or empty");
        }
        // Logic to save the university
        universityRepository.save(university);
        return ResponseEntity.ok(university);
    }
    @Operation(summary = "Update University", description = "Modifies the setting of the university after it has been created")
    @PutMapping("/universities/universityName/{universityName}")
    public ResponseEntity<Object> updateUniversityName(@PathVariable String universityName, @RequestBody University request) {
        University university = universityRepository.findByUniversityName(universityName);

        if (university == null) {
            String message = "University with name " + universityName + " not found.";
            return ResponseEntity.status(404).body("{\"message\":\"" + message + "\"}");
        }

        StringBuilder updateLog = new StringBuilder("Updating university details:");
        boolean updated = false;

        // Check if the university name should be updated
        if (request.getUniversityName() != null && !request.getUniversityName().equals(university.getUniversityName())) {
            updateLog.append("\n - University name changed from '")
                    .append(university.getUniversityName())
                    .append("' to '")
                    .append(request.getUniversityName())
                    .append("'.");
            university.setUniversityName(request.getUniversityName());
            updated = true;
        }

        // Check if the university URL should be updated
        if (request.getUniversityUrl() != null && !request.getUniversityUrl().equals(university.getUniversityUrl())) {
            updateLog.append("\n - University URL changed from '")
                    .append(university.getUniversityUrl())
                    .append("' to '")
                    .append(request.getUniversityUrl())
                    .append("'.");
            university.setUniversityUrl(request.getUniversityUrl());
            updated = true;
        }

        if (updated) {
            universityRepository.save(university);
            universityRepository.flush();
            return ResponseEntity.ok().body("{\"message\":\"University updated successfully.\", \"details\":\"" + updateLog.toString() + "\"}");
        } else {
            String noUpdateMessage = "No changes were made to the university details for '" + universityName + "'.";
            return ResponseEntity.ok().body("{\"message\":\"" + noUpdateMessage + "\"}");
        }
    }

    @Operation(summary = "Delete University", description = "deletes a university based on the university name")
    @DeleteMapping(path = "/universities/{universityName}")
    String deleteUniversityByName(@PathVariable String universityName) {
        universityRepository.deleteByUniversityName(universityName);
        return success;
    }
}
