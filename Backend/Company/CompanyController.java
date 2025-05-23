package onetoone.Company;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.HomePage.CompanyUploadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Doyle Chism
 */
@RestController
@Tag(name = "Company Controller", description = "REST APIs related to Company Entity")
public class CompanyController {

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    CompanyUploadRepository companyUploadRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @Operation(summary = "Get all companies", description = "Retrieve all the companies information from the database")
    @GetMapping(path = "/company")
    List<Company> getAllCompany() {
        return companyRepository.findAll();
    }

    @Operation(summary = "Get company by company Name", description = "Retrieve a company based on their name")
    @GetMapping(path = "/company/findByName/{companyName}")
    Company getCompanyByCompanyName(@PathVariable String companyName) {
        return companyRepository.findByCompanyName(companyName);
    }

    @Operation(summary = "Create new company", description = "Upload a new company to the database")
    @PostMapping(path = "/company")
    public ResponseEntity<String> createCompany(@RequestBody Company company) {

        if (company == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Company existingCompany = companyRepository.findByCompanyName(company.getCompanyName());
        if (existingCompany != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"message\":\"CompanyName already exists\"}");
        }
        companyRepository.save(company);
        return ResponseEntity.status(HttpStatus.CREATED).body("{\"message\":\"Company created successfully\"}");
    }

    @Operation(summary = "Check company by company name", description = "Checks if a company exists in the database when signing up")
    @PostMapping(path = "/company/checkid")
    boolean checkCompanyName(@RequestBody Company company) {
        Company existingCompany = companyRepository.findByCompanyName(company.getCompanyName());
        if (existingCompany != null || company.getCompanyName().isEmpty()) {
            return false;
        }
        return true;
    }
    @Operation(summary = "Login Company", description = "Checks if the company exists in the database to login")
    @PostMapping(path = "/company/login/{companyName}")
    ResponseEntity<Map<String, Object>> loginCompany(@RequestBody Company company) {

        Map<String, Object> response = new HashMap<>();
        try {
            Company existingCompany = companyRepository.findByCompanyName(company.getCompanyName());
            if (existingCompany == null) {
                response.put("result", false);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            if (!existingCompany.getCompanyPassword().equals(company.getCompanyPassword())) {
                response.put("result", false);
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            String companyName = existingCompany.getCompanyName();
            response.put("companyName", companyName);
            //check if needed
//            response.put("companyID", existingCompany.getCom);
            response.put("result", true);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put(failure, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Operation(summary = "Updates the company information", description = "updates the setting details of the company")
    @PutMapping(path = "/company/update/{companyName}")
    Company updateCompany(@PathVariable String companyName, @RequestBody Company request) {
        Company company = companyRepository.findByCompanyName(companyName);
        if (company == null) {
            return null;
        }

        if (request.getCompanyName() != null) {
            company.setCompanyName(request.getCompanyName());
        }
        if (request.getCompanyPassword() != null) {
            company.setCompanyPassword(request.getCompanyPassword());
        }
        if (request.getCompanyEmail() != null) {
            company.setCompanyEmail(request.getCompanyEmail());
        }
        if (request.getCompanyPhoneNumber() != null) {
            company.setCompanyPhoneNumber(request.getCompanyPhoneNumber());
        }
        if (request.getCountryOrigin() != null) {
            company.setCountryOrigin(request.getCountryOrigin());
        }
        if (request.getCompanyMoto() != null) {
            company.setCompanyMoto(request.getCompanyMoto());
        }
        if (request.getCompanyDescription() != null) {
            company.setCompanyDescription(request.getCompanyDescription());
        }
        if (request.getdateFounded() != null) {
            company.setdateFounded(request.getdateFounded());
        }
        companyRepository.save(company);
        companyRepository.flush();
        return companyRepository.findByCompanyName(companyName);
    }
    @Operation(summary = "Delete Company", description = "deletes a company based on the company name")
    @DeleteMapping(path = "/company/delete/{companyName}")
    String deleteCompany(@PathVariable String companyName) {
        companyRepository.deleteByCompanyName(companyName);
        companyRepository.flush();
        return success;
    }


}
