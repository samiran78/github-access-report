package com.samiran.github_access_report.controller;


import com.samiran.github_access_report.model.AccessReport;
import com.samiran.github_access_report.service.AccessReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "GitHub Access Report")
public class AccessReportController {
private final AccessReportService accessReportService;
@GetMapping("/access-report")
@Operation(summary = "Generate access report for a GitHub organization")
public ResponseEntity<AccessReport> getAccessReport(@RequestParam String organization){
    //Add validation and sanitization
    if(organization==null || organization.trim().isEmpty()){
        log.warn("Invalid organization input");
       throw  new IllegalArgumentException("Organization name must not be empty");
    }
        log.info("Report requested for org: {}", organization);
        AccessReport report = accessReportService.generateReport(organization.trim());
        return ResponseEntity.ok(report);
}

}
