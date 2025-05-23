package onetoone.University;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UniDataLoaderRunner implements CommandLineRunner {

    @Autowired
    private DataLoaderUniversity dataLoaderUniversity;

    @Override
    public void run(String... args) throws Exception {
        String filePath = "src/main/resources/us_universities.xls";
//        String filePath = "/tmp/us_universities.xls";

        // Call the asynchronous method to load universities
        dataLoaderUniversity.loadUniversities(filePath);

        // You can log or print something here to confirm that the task started asynchronously
        System.out.println("University data loading started in the background...");
    }
}
