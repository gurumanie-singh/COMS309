package onetoone.Degrees;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoaderRunner implements CommandLineRunner {

    @Autowired
    private DataLoaderDegreeMajors dataLoaderDegreeMajors;

    @Override
    public void run(String... args) throws Exception {
        // Specify the file path for the Excel file
        String filePath = "src/main/resources/DegreeData.xls";
        //String filePath = "/tmp/DegreeData.xls";
        // Call the loadDegrees method asynchronously
        dataLoaderDegreeMajors.loadDegrees(filePath);

        // Log or print to confirm that the task started asynchronously
        System.out.println("Degree data loading started in the background...");
    }
}
