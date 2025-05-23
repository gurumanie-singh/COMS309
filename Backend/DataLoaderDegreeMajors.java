package onetoone.Degrees;

import jakarta.transaction.Transactional;
import org.apache.poi.hssf.usermodel.HSSFWorkbook; // Import HSSFWorkbook for .xls
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;  // Import Async
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class DataLoaderDegreeMajors {

    private static final Logger logger = LoggerFactory.getLogger(DataLoaderDegreeMajors.class);

    @Autowired
    private DegreesRepository degreesRepository;

    // Define batch size
    private static final int BATCH_SIZE = 500;

    @Async
    @Transactional
    public CompletableFuture<Void> loadDegrees(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new HSSFWorkbook(fis)) { // Use HSSFWorkbook for .xls files

            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
            List<Degrees> batch = new ArrayList<>();
            Set<String> existingDegrees = new HashSet<>(); // Cache for degree names already in DB

            // Load all existing degree names into the cache
            degreesRepository.findAll().forEach(degree -> existingDegrees.add(degree.getDegreeName()));

            int totalRows = 0, skippedRows = 0;

            // Iterate through the rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Start from 1 to skip header
                Row row = sheet.getRow(i);
                if (row == null) continue; // Skip empty rows

                String degreeName = row.getCell(0).getStringCellValue().trim();

                // Skip if the degree already exists
                if (existingDegrees.contains(degreeName)) {
                    skippedRows++;
                    continue;
                }

                // Add new degree to batch
                batch.add(new Degrees(degreeName));
                existingDegrees.add(degreeName); // Add to cache to avoid duplicate checks
                totalRows++;

                // Process batch if it reaches the defined batch size
                if (batch.size() >= BATCH_SIZE) {
                    saveBatch(batch);
                }
            }

            // Save any remaining degrees in the batch
            if (!batch.isEmpty()) {
                saveBatch(batch);
            }

            logger.info("Degree loading completed: Total rows processed: {}, Skipped: {}", totalRows, skippedRows);

        } catch (IOException e) {
            logger.error("Error loading degrees from file: {}", e.getMessage(), e);
            throw new RuntimeException("Error loading degrees from file: " + e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null); // Return a completed future
    }

    // Helper method to save a batch of degrees
    private void saveBatch(List<Degrees> batch) {
        degreesRepository.saveAll(batch); // Bulk insert into the database
        logger.info("Batch of {} degrees saved successfully.", batch.size());
        batch.clear(); // Clear the batch for the next set
    }
}
