package com.example.hrmsclient.repository;

import com.example.hrmsclient.entity.Applicant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
    Page<Applicant> findByJobPostingId(Long jobId, Pageable pageable);
    Page<Applicant> findByStage(String stage, Pageable pageable);
    List<Applicant> findByJobPostingIdAndStage(Long jobId, String stage);
    long countByJobPostingId(Long jobId);
    long countByJobPostingIdAndStage(Long jobId, String stage);

    @Query("SELECT a.stage, COUNT(a) FROM Applicant a WHERE a.jobPosting.id = :jobId GROUP BY a.stage")
    List<Object[]> countByStageForJob(@Param("jobId") Long jobId);
}