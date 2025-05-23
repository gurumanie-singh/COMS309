package onetoone.CareerClusters;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CareerClustersRepository extends JpaRepository<CareerClusters, Long> {
    CareerClusters findById(int id);

    CareerClusters findByClusterName(String clusterName);

    List<CareerClusters> findByClusterNameIn(List<String> clusterNames);

    @Transactional
    void deleteById(int id);
}

