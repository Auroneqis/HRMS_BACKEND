package com.example.hrmsclient.controller;

import com.example.hrmsclient.entity.Applicant;
import com.example.hrmsclient.entity.JobPosting;
import com.example.hrmsclient.service.RecruitmentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * BASE PATH: /api/recruitment
 *
 * Jobs:
 *   POST   /api/recruitment/jobs                     — create job posting
 *   GET    /api/recruitment/jobs                     — list jobs (filter by status)
 *   GET    /api/recruitment/jobs/{id}                — get one job
 *   PATCH  /api/recruitment/jobs/{id}/status         — open/close/pause a job
 *
 * Applicants:
 *   POST   /api/recruitment/jobs/{jobId}/applicants  — add applicant
 *   GET    /api/recruitment/applicants               — list applicants (filter jobId/stage)
 *   GET    /api/recruitment/applicants/{id}          — get one applicant
 *   PATCH  /api/recruitment/applicants/{id}/advance  — move to next stage
 *   PATCH  /api/recruitment/applicants/{id}/reject   — reject
 *   PATCH  /api/recruitment/applicants/{id}          — update fields
 *
 * Pipeline:
 *   GET    /api/recruitment/jobs/{id}/pipeline       — stage-wise count
 */
@RestController
@RequestMapping("/api/recruitment")
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    public RecruitmentController(RecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
    }

    // ── Jobs ──────────────────────────────────────────────────────────────────

    @PostMapping("/jobs")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> createJob(@RequestBody JobPosting job,
                                       @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(Map.of("status", "success",
            "data", recruitmentService.createJob(job, user.getUsername())));
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<?> listJobs(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<JobPosting> result = recruitmentService.listJobs(status, page, size);
        return ResponseEntity.ok(Map.of("status", "success",
            "totalElements", result.getTotalElements(), "data", result.getContent()));
    }

    @GetMapping("/jobs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<?> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("status", "success",
            "data", recruitmentService.getJob(id)));
    }

    @PatchMapping("/jobs/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> updateJobStatus(@PathVariable Long id,
                                             @RequestParam String status) {
        return ResponseEntity.ok(Map.of("status", "success",
            "data", recruitmentService.updateJobStatus(id, status)));
    }

    // ── Applicants ────────────────────────────────────────────────────────────

    @PostMapping("/jobs/{jobId}/applicants")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> addApplicant(@PathVariable Long jobId,
                                          @RequestBody Applicant applicant) {
        return ResponseEntity.ok(Map.of("status", "success",
            "data", recruitmentService.addApplicant(jobId, applicant)));
    }

    @GetMapping("/applicants")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<?> listApplicants(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String stage,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Applicant> result = recruitmentService.listApplicants(jobId, stage, page, size);
        return ResponseEntity.ok(Map.of("status", "success",
            "totalElements", result.getTotalElements(), "data", result.getContent()));
    }

    @GetMapping("/applicants/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<?> getApplicant(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("status", "success",
            "data", recruitmentService.getApplicant(id)));
    }

    @PatchMapping("/applicants/{id}/advance")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> advanceStage(@PathVariable Long id,
                                          @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(Map.of("status", "success",
            "data", recruitmentService.advanceStage(id, notes)));
    }

    @PatchMapping("/applicants/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> rejectApplicant(@PathVariable Long id,
                                             @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(Map.of("status", "success",
            "data", recruitmentService.rejectApplicant(id, body.get("reason"))));
    }

    @PatchMapping("/applicants/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> updateApplicant(@PathVariable Long id,
                                             @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(Map.of("status", "success",
            "data", recruitmentService.updateApplicant(id, body)));
    }

    // ── Pipeline ──────────────────────────────────────────────────────────────

    @GetMapping("/jobs/{id}/pipeline")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public ResponseEntity<?> pipeline(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("status", "success",
            "data", recruitmentService.getPipelineSummary(id)));
    }
}