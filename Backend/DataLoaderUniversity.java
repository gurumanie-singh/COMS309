package onetoone.University;

import jakarta.transaction.Transactional;
import org.apache.poi.hssf.usermodel.HSSFWorkbook; // Import HSSFWorkbook for .xls files
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
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class DataLoaderUniversity {

    private static final Logger logger = LoggerFactory.getLogger(DataLoaderUniversity.class);

    @Autowired
    private UniversityRepository universityRepository;

    // Define batch size
    private static final int BATCH_SIZE = 500;

    @Async
    @Transactional
    public CompletableFuture<Void> loadUniversities(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new HSSFWorkbook(fis)) { // Use HSSFWorkbook for .xls files

            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
            List<University> batch = new ArrayList<>();
            Set<String> existingUniversities = new HashSet<>(); // Cache for university names already in DB

            // Load all existing university names into the cache
            universityRepository.findAll().forEach(university -> existingUniversities.add(university.getUniversityName()));

            int totalRows = 0, skippedRows = 0;

            // Iterate through the rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Start from 1 to skip header
                Row row = sheet.getRow(i);
                if (row == null) continue; // Skip empty rows

                String universityName = Optional.ofNullable(row.getCell(0))
                        .map(cell -> cell.getStringCellValue().trim())
                        .orElse(""); // Safely get university name
                String universityUrl = Optional.ofNullable(row.getCell(1))
                        .map(cell -> cell.getStringCellValue().trim())
                        .orElse(""); // Safely get university URL

                // Skip if university name is empty or already exists
                if (universityName.isEmpty() || existingUniversities.contains(universityName)) {
                    skippedRows++;
                    continue;
                }

                // Add new university to batch
                University university = new University();
                university.setUniversityName(universityName);
                university.setUniversityUrl(universityUrl);
                batch.add(university);
                existingUniversities.add(universityName); // Add to cache to avoid duplicate checks
                totalRows++;

                // Process batch if it reaches the defined batch size
                if (batch.size() >= BATCH_SIZE) {
                    saveBatch(batch);
                }
            }

            // Save any remaining universities in the batch
            if (!batch.isEmpty()) {
                saveBatch(batch);
            }

            logger.info("University loading completed: Total rows processed: {}, Skipped: {}", totalRows, skippedRows);

        } catch (IOException e) {
            logger.error("Error loading universities from file: {}", e.getMessage(), e);
            throw new RuntimeException("Error loading universities from file: " + e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null); // Return a completed future
    }

    // Helper method to save a batch of universities
    private void saveBatch(List<University> batch) {
        universityRepository.saveAll(batch); // Bulk insert into the database
        logger.info("Batch of {} universities saved successfully.", batch.size());
        batch.clear(); // Clear the batch for the next set
    }
}
