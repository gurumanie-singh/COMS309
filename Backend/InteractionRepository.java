package onetoone.HomePage;

import onetoone.Company.Company;
import onetoone.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    //    //findbyUsername
    Interaction findInteractionById(long id);

    Interaction findByUsername(User username);

    Interaction findByCompanyUpload(CompanyUpload companyUpload);

}
