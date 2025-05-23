package onetoone.Degrees;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface DegreesRepository extends JpaRepository<Degrees, Long> {

    Degrees findByDegreeName(String name);

    List<Degrees> findByDegreeNameContaining(String degreeName);

    @Transactional
    void deleteByDegreeName(String degreeName);
}

