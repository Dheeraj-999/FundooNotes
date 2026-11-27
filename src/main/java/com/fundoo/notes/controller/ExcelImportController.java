package com.fundoo.notes.controller;

/*
 * Class Name  : ExcelImportController
 * Description : Securely handles bulk note imports via Excel (.xlsx) file uploads,
 *               validating authentication and triggering the Spring Batch import pipeline.
 * Author      : Dheeraj Buchha * Date        : 27/11/2026
 */

import com.fundoo.notes.entity.User;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.TokenService;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/batch")
public class ExcelImportController {

    private final JobLauncher jobLauncher;
    private final Job excelNoteImportJob;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public ExcelImportController(
            JobLauncher jobLauncher,
            @Qualifier("excelNoteImportJob") Job excelNoteImportJob,
            TokenService tokenService,
            UserRepository userRepository) {

        this.jobLauncher = jobLauncher;
        this.excelNoteImportJob = excelNoteImportJob;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    /**
     * Imports user notes in bulk from an uploaded Excel spreadsheet (.xlsx).
     *
     * @param authorization authorization header containing the Bearer JWT token
     * @param file          multipart Excel document
     * @return execution status metadata
     * @throws Exception on processing or batch launching failure
     */
    @PostMapping("/import/notes")
    public ResponseEntity<Map<String, Object>> importNotes(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("file") MultipartFile file) throws Exception {

        String jwtToken = extractBearerToken(authorization);
        if (jwtToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid authorization header format"));
        }

        if (!tokenService.isTokenValid(jwtToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token"));
        }

        String userEmail = tokenService.extractSubject(jwtToken);
        User authenticatedUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please upload a non-empty Excel file"));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only .xlsx files are supported"));
        }

        Path temporaryImportFile = Files.createTempFile("fundoo-notes-import-", ".xlsx");

        try {
            file.transferTo(temporaryImportFile);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("filePath", temporaryImportFile.toAbsolutePath().toString())
                    .addLong("userId", authenticatedUser.getId())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution batchExecution = jobLauncher.run(excelNoteImportJob, jobParameters);

            return ResponseEntity.ok(Map.of(
                    "message", "Excel import job started successfully",
                    "executionId", batchExecution.getId(),
                    "status", batchExecution.getStatus().toString()
            ));

        } finally {
            try {
                Files.deleteIfExists(temporaryImportFile);
            } catch (Exception ignored) {
                // Temporary file cleanup handled on exit
            }
        }
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7).trim();
        }
        return null;
    }
}
