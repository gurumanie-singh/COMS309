package onetoone.Notifications;

import jakarta.transaction.Transactional;
import onetoone.Company.Company;
import onetoone.HomePage.CompanyUpload;
import onetoone.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NotificationsRepository extends JpaRepository<Notifications, Integer> {


    List<Notifications> findById(int id);

//    List<Notifications> findByUsername(String username);

    List<Notifications> findByCompany(Company company);

    List<Notifications> findByUser(User user);

    List<Notifications> findByCompanyUpload(CompanyUpload companyUpload);

    @Transactional
    List<Notifications> deleteByUser(User user);


}
