package com.fundoo.notes.controller;

/*
 * Class Name  : BatchJobController
 * Description : REST controller providing manual triggering and monitoring
 *               endpoints for Spring Batch data processing jobs.
 * Author      : Dheeraj Buchha
 * Note        : Refactored and maintained for FundooNotes (Original: Utkarsh Jain)
 * Date        : 27/11/2026
 */

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/batch")
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final Job noteProcessingJob;

    public BatchJobController(
            JobLauncher jobLauncher,
            Job noteProcessingJob) {
        this.jobLauncher = jobLauncher;
        this.noteProcessingJob = noteProcessingJob;
    }

    /**
     * Triggers asynchronous execution of the note processing Spring Batch job.
     *
     * @return response containing job details, instance ID, and initial execution status
     * @throws Exception if job launching fails
     */
    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runBatchJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(noteProcessingJob, jobParameters);

        return ResponseEntity.ok(Map.of(
                "jobName", jobExecution.getJobInstance().getJobName(),
                "executionId", jobExecution.getId(),
                "status", jobExecution.getStatus().toString()
        ));
    }
}
