// ── JobPostingRepository.java ─────────────────────────────────────────────────
package com.example.hrmsclient.repository;

import com.example.hrmsclient.entity.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    Page<JobPosting> findByStatus(String status, Pageable pageable);
    List<JobPosting> findByDepartmentAndStatus(String department, String status);
    long countByStatus(String status);
}