package com.example.hrmsclient.service;

import com.example.hrmsclient.entity.*;
import com.example.hrmsclient.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RecruitmentService {

    private static final Logger log = LoggerFactory.getLogger(RecruitmentService.class);

    // ATS pipeline order
    private static final List<String> PIPELINE_STAGES = List.of(
        "APPLIED", "SCREENING", "INTERVIEW", "TECHNICAL", "HR_ROUND", "OFFER", "HIRED"
    );

    private final JobPostingRepository  jobRepo;
    private final ApplicantRepository   applicantRepo;

    public RecruitmentService(JobPostingRepository jobRepo,
                               ApplicantRepository applicantRepo) {
        this.jobRepo       = jobRepo;
        this.applicantRepo = applicantRepo;
    }

    // ── Job Postings ──────────────────────────────────────────────────────────

    @Transactional
    public JobPosting createJob(JobPosting job, String createdBy) {
        job.setCreatedBy(createdBy);
        job.setStatus("DRAFT");
        return jobRepo.save(job);
    }

    @Transactional
    public JobPosting updateJobStatus(Long jobId, String newStatus) {
        JobPosting job = jobRepo.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        job.setStatus(newStatus.toUpperCase());
        return jobRepo.save(job);
    }

    public Page<JobPosting> listJobs(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return status != null ? jobRepo.findByStatus(status.toUpperCase(), pageable)
                              : jobRepo.findAll(pageable);
    }

    public JobPosting getJob(Long id) {
        return jobRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Job not found: " + id));
    }

    // ── Applicants ────────────────────────────────────────────────────────────

    @Transactional
    public Applicant addApplicant(Long jobId, Applicant applicant) {
        JobPosting job = jobRepo.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        if (!"OPEN".equals(job.getStatus())) {
            throw new IllegalStateException("Job posting is not open for applications");
        }

        applicant.setJobPosting(job);
        applicant.setStage("APPLIED");
        applicant.setStatus("ACTIVE");
        return applicantRepo.save(applicant);
    }

    /**
     * Move applicant to next pipeline stage.
     */
    @Transactional
    public Applicant advanceStage(Long applicantId, String notes) {
        Applicant a = applicantRepo.findById(applicantId)
            .orElseThrow(() -> new RuntimeException("Applicant not found: " + applicantId));

        int currentIdx = PIPELINE_STAGES.indexOf(a.getStage());
        if (currentIdx < 0 || currentIdx >= PIPELINE_STAGES.size() - 1) {
            throw new IllegalStateException("Applicant is already at final stage: " + a.getStage());
        }

        String nextStage = PIPELINE_STAGES.get(currentIdx + 1);
        a.setStage(nextStage);
        if (notes != null) a.setNotes(notes);

        if ("HIRED".equals(nextStage)) {
            a.setStatus("HIRED");
        }

        log.info("ATS | applicant {} moved to stage {}", applicantId, nextStage);
        return applicantRepo.save(a);
    }

    /**
     * Reject an applicant with a reason.
     */
    @Transactional
    public Applicant rejectApplicant(Long applicantId, String reason) {
        Applicant a = applicantRepo.findById(applicantId)
            .orElseThrow(() -> new RuntimeException("Applicant not found: " + applicantId));
        a.setStatus("REJECTED");
        a.setStage("REJECTED");
        a.setNotes(reason);
        return applicantRepo.save(a);
    }

    /**
     * Update any field (stage, notes, assignedTo, etc.)
     */
    @Transactional
    public Applicant updateApplicant(Long applicantId, Map<String, String> updates) {
        Applicant a = applicantRepo.findById(applicantId)
            .orElseThrow(() -> new RuntimeException("Applicant not found: " + applicantId));

        if (updates.containsKey("stage"))      a.setStage(updates.get("stage").toUpperCase());
        if (updates.containsKey("status"))     a.setStatus(updates.get("status").toUpperCase());
        if (updates.containsKey("notes"))      a.setNotes(updates.get("notes"));
        if (updates.containsKey("assignedTo")) a.setAssignedTo(updates.get("assignedTo"));

        return applicantRepo.save(a);
    }

    public Page<Applicant> listApplicants(Long jobId, String stage, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        if (jobId != null)   return applicantRepo.findByJobPostingId(jobId, pageable);
        if (stage != null)   return applicantRepo.findByStage(stage.toUpperCase(), pageable);
        return applicantRepo.findAll(pageable);
    }

    public Applicant getApplicant(Long id) {
        return applicantRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Applicant not found: " + id));
    }

    // ── Pipeline summary ──────────────────────────────────────────────────────

    public Map<String, Object> getPipelineSummary(Long jobId) {
        List<Object[]> rows = applicantRepo.countByStageForJob(jobId);
        Map<String, Long> byStage = new LinkedHashMap<>();
        for (String s : PIPELINE_STAGES) byStage.put(s, 0L);

        for (Object[] row : rows) {
            byStage.put((String) row[0], (Long) row[1]);
        }

        return Map.of(
            "jobId",       jobId,
            "total",       applicantRepo.countByJobPostingId(jobId),
            "byStage",     byStage,
            "pipelineOrder", PIPELINE_STAGES
        );
    }
}