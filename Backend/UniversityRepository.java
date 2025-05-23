package onetoone.University;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UniversityRepository extends JpaRepository<University, Long> {
    University findById(int id);

    University findByUniversityName(String universityNameID);

    boolean existsByUniversityName(String universityName);

    @Transactional
    void deleteByUniversityName(String universityName);

    List<University> findByUniversityNameContaining(String universityName);

}
