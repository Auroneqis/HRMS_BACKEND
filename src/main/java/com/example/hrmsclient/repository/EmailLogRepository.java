package com.example.hrmsclient.repository;

import com.example.hrmsclient.entity.EmailLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    Page<EmailLog> findByRecipientEmailContainingIgnoreCase(
        String email, Pageable pageable);

    Page<EmailLog> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);
}