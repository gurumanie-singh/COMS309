package onetoone.CareerClusters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataLoaderCareerClusters implements CommandLineRunner {

    @Autowired
    private CareerClustersRepository CareerClustersRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if clusters already exist to avoid re-creation
        if (CareerClustersRepository.count() == 0) {
            // Define career clusters
            List<CareerClusters> clusters = Arrays.asList(
                    new CareerClusters("Agriculture, Food and Natural Resources", "Careers related to the production, processing, marketing, distribution, financing, and development of agricultural commodities and resources."),
                    new CareerClusters("Architecture and Construction", "Careers in designing, planning, managing, building, and maintaining structures and infrastructure."),
                    new CareerClusters("Arts, A/V Technology and Communications", "Careers in designing, producing, exhibiting, performing, writing, and publishing multimedia content."),
                    new CareerClusters("Business Management and Administration", "Careers that involve planning, organizing, directing, and evaluating business functions essential to efficient and productive operations."),
                    new CareerClusters("Education and Training", "Careers in planning, managing, and providing education and training services, and related learning support services."),
                    new CareerClusters("Finance", "Careers in planning, managing, and providing banking, investment, financial planning, insurance, and business financial management services."),
                    new CareerClusters("Government and Public Administration", "Careers in planning and performing governmental functions at the local, state, and federal levels, including governance and national security."),
                    new CareerClusters("Health Science", "Careers in planning, managing, and providing therapeutic services, diagnostic services, health informatics, and support services."),
                    new CareerClusters("Hospitality and Tourism", "Careers in managing, marketing, and operating restaurants, hotels, amusement parks, and other entertainment venues."),
                    new CareerClusters("Human Services", "Careers in providing family and personal services such as counseling, mental health services, and personal care."),
                    new CareerClusters("Information Technology", "Careers related to the design, development, support, and management of hardware, software, multimedia, and systems integration services."),
                    new CareerClusters("Law, Public Safety, Corrections & Security", "Careers in planning, managing, and providing legal, public safety, protective, and homeland security services."),
                    new CareerClusters("Manufacturing", "Careers in planning, managing, and performing the processing of materials into intermediate or final products."),
                    new CareerClusters("Marketing", "Careers in planning, managing, and performing marketing activities to reach organizational objectives, including promotion and sales."),
                    new CareerClusters("Science, Technology, Engineering & Mathematics", "Careers in planning and providing scientific research, as well as applying scientific principles to solve problems."),
                    new CareerClusters("Transportation, Distribution & Logistics", "Careers in planning, managing, and moving people, materials, and goods by road, air, rail, and water.")
            );

            // Save clusters to the repository
            CareerClustersRepository.saveAll(clusters);
        }
    }
}


