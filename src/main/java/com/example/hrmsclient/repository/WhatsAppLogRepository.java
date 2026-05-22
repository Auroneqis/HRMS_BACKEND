package com.example.hrmsclient.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.hrmsclient.entity.WhatsAppLog;

@Repository
public interface WhatsAppLogRepository extends JpaRepository<WhatsAppLog, Long> {

    // All logs for a specific phone number
    List<WhatsAppLog> findByToPhoneOrderBySentAtDesc(String toPhone);

    // All logs by status (SENT / FAILED / PENDING)
    Page<WhatsAppLog> findByStatusOrderBySentAtDesc(String status, Pageable pageable);

    // All logs by message type (PAYSLIP / LEAVE / ATTENDANCE etc.)
    Page<WhatsAppLog> findByMessageTypeOrderBySentAtDesc(String messageType, Pageable pageable);

    // Count failures in last N hours (for admin dashboard / alerting)
    @Query("SELECT COUNT(w) FROM WhatsAppLog w WHERE w.status = 'FAILED' AND w.sentAt >= :since")
    long countFailuresSince(@Param("since") LocalDateTime since);

    // Recent logs for admin view
    Page<WhatsAppLog> findAllByOrderBySentAtDesc(Pageable pageable);
}
