package onetoone.CareerClusters;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CareerClustersRepository extends JpaRepository<CareerClusters, Long> {
    CareerClusters findById(int id);

    @Transactional
    void deleteById(int id);
}

