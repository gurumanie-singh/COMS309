package onetoone.CareerClusters;


import onetoone.Company.CompanyRepository;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
public class CareerClustersController {

    @Autowired
    CareerClustersRepository CareerClustersRepository;

    @Autowired
    UserRepository userRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @GetMapping(path = "/careerClusters")
    List<CareerClusters> getAllCareerClusters() {
        return CareerClustersRepository.findAll();
    }

    @GetMapping(path = "/careerClusters/{id}")
    CareerClusters getUserById(@PathVariable int id) {
        return CareerClustersRepository.findById(id);
    }

    @PostMapping(path = "/careerClusters")
    String createCareerClusters(@RequestBody CareerClusters CareerClusters) {
        if (CareerClusters == null)
            return failure;
        CareerClustersRepository.save(CareerClusters);
        return success;
    }

    @PutMapping("/careerClusters/{id}")
    CareerClusters updateCareerClusters(@PathVariable int id, @RequestBody CareerClusters request) {
        CareerClusters careerClusters = CareerClustersRepository.findById(id);
        if (careerClusters == null)
            return null;

        if (request.getClusterID() != 0) {
            careerClusters.setClusterID(request.getClusterID());
        }
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


    @DeleteMapping(path = "/careerClusters/{id}")
    String deleteCareerClusters(@PathVariable int id) {
        CareerClustersRepository.deleteById(id);
        return success;
    }

}

