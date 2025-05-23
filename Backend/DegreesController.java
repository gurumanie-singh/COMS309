package onetoone.Degrees;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@Tag(name = "Degrees Controller", description = "REST APIs related to Degrees Entity")
public class DegreesController {

    @Autowired
    DegreesRepository degreesRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    // Fetch all degrees
    @Operation(summary = "Get all degrees", description = "Retrieve a list of all degrees from the database.")
    @GetMapping(path = "/degrees")
    List<Degrees> getAllDegrees() {
        return degreesRepository.findAll();
    }

    @Operation(summary = "Get degrees by name", description = "Search for degrees by name. Optionally, specify if the search should be exact.")
    @GetMapping(path = "/degrees/search/{degreeName}")
    public List<Degrees> getDegreesByName(@PathVariable String degreeName, @RequestParam(defaultValue = "false") boolean exact) {
        if (exact) {
            Degrees degree = degreesRepository.findByDegreeName(degreeName);
            return degree != null ? List.of(degree) : List.of();
        } else {
            return degreesRepository.findByDegreeNameContaining(degreeName);
        }
    }

    // Create a new degree
    @Operation(summary = "Create a new degree", description = "Add a new degree to the database.")
    @PostMapping(path = "/degrees")
    String createDegree(@RequestBody Degrees degree) {
        if (degree == null) {
            return failure;
        }

        degreesRepository.save(degree);
        return success;
    }

    // Update a degree by degree name
    @Operation(summary = "Update degree by name", description = "Update the details of a degree using the degree name.")
    @PutMapping("/degrees/{degreeName}")
    Degrees updateDegreeByName(@PathVariable String degreeName, @RequestBody Degrees request) {
        Degrees degree = degreesRepository.findByDegreeName(degreeName);

        if (degree == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Degree not found");
        }

        // Update the degree name if provided in the request
        if (request.getDegreeName() != null) {
            degree.setDegreeName(request.getDegreeName());
        }

        degreesRepository.save(degree);
        return degree;
    }

    // Delete a degree by degree name
    @Operation(summary = "Delete degree by name", description = "Delete a degree from the database by its name.")
    @DeleteMapping(path = "/degrees/{degreeName}")
    String deleteDegree(@PathVariable String degreeName) {
        Degrees degree = degreesRepository.findByDegreeName(degreeName);
        if (degree == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Degree not found");
        }

        degreesRepository.deleteByDegreeName(degreeName);
        return success;
    }
}
