package onetoone.CareerClusters;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Company.CompanyRepository;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@Tag(name = "CareerClusters Controller", description = "REST APIs related to CareerClusters Entity")
public class CareerClustersController {

    @Autowired
    CareerClustersRepository CareerClustersRepository;

    @Autowired
    UserRepository userRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @Operation(summary = "Get all career clusters", description = "Retrieve a list of all career clusters from the database.")
    @GetMapping(path = "/careerClusters")
    List<CareerClusters> getAllCareerClusters() {
        return CareerClustersRepository.findAll();
    }

    @Operation(summary = "Get career cluster by ID", description = "Retrieve a specific career cluster using its ID.")
    @GetMapping(path = "/careerClusters/{id}")
    CareerClusters getClusterById(@PathVariable int id) {
        return CareerClustersRepository.findById(id);
    }

    @Operation(summary = "Get career cluster by name", description = "Retrieve a specific career cluster using its name.")
    @GetMapping("/careerClusters/clusters/{clusterName}")
    public CareerClusters getClusterByClusterName(@PathVariable String clusterName) {
        CareerClusters cluster = CareerClustersRepository.findByClusterName(clusterName);
        if (cluster == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cluster not found");
        }
        return cluster;
    }

    @Operation(summary = "Create career clusters", description = "Add one or more new career clusters to the database.")
    @PostMapping(path = "/careerClusters")
    String createCareerClusters(@RequestBody List<CareerClusters> careerClusters) {
        if (careerClusters == null || careerClusters.isEmpty()) {
            return failure;
        }
        CareerClustersRepository.saveAll(careerClusters);
        return success;
    }

    @Operation(summary = "Update career cluster", description = "Update the details of an existing career cluster by its ID.")
    @PutMapping("/careerClusters/{id}")
    CareerClusters updateCareerClusters(@PathVariable int id, @RequestBody CareerClusters request) {
        CareerClusters careerClusters = CareerClustersRepository.findById(id);
        if (careerClusters == null)
            return null;

        if (request.getClusterName() != null) {
            careerClusters.setClusterName(request.getClusterName());
        }
        if (request.getClusterDescription() != null) {
            careerClusters.setClusterDescription(request.getClusterDescription());
        }
        CareerClustersRepository.save(careerClusters);
        CareerClustersRepository.flush();
        return CareerClustersRepository.findById(id);
    }

    @Operation(summary = "Delete career cluster", description = "Delete a specific career cluster using its ID.")
    @DeleteMapping(path = "/careerClusters/{id}")
    String deleteCareerClusters(@PathVariable int id) {
        CareerClustersRepository.deleteById(id);
        return success;
    }

}

