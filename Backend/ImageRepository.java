package onetoone.ProfilePhoto;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Image findById(int id);
    Image findByUser_Username(String username);

}
