package onetoone.Users;


import onetoone.CareerClusters.CareerClusters;
import onetoone.Degrees.Degrees;
import onetoone.University.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Vivek Bengre
 */

public interface UserRepository extends JpaRepository<User, Long> {
    User findById(int id);

   // UserName findByUser_Name(String username);
    User findByUsername(String username);  // Custom query to find User by username

    User findByUserPassword(String userPassword);

    List<User> findByUniversity(University university);

    List<User> findByDegrees(Degrees degree);

    List<User> findByCareerClusters(CareerClusters careerClusters);

    @Transactional
    void deleteById(int id);

    @Transactional
    void deleteByUsername(String username);

}
