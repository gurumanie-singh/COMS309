package onetoone.Company;


import onetoone.HomePage.CompanyUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
//    Company findById(int id);

    Company findByCompanyName(String companyName);

//    @Transactional
//    void deleteById(int id);

    @Transactional
    void deleteByCompanyName(String companyName);
}
