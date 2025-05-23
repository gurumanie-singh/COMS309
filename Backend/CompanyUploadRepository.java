package onetoone.HomePage;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface CompanyUploadRepository extends JpaRepository<CompanyUpload, Long> {

    //find by uploadId
    CompanyUpload findById(int id);

    CompanyUpload findByPostId(String postId);
    //find by companyName


//    CompanyUpload findByUploadId(String uploadId);
    //find by UploadTitle

    //delete by UploadTitle
}

