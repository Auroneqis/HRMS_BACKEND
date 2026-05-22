// ── Shift.java ────────────────────────────────────────────────────────────────
package com.example.hrmsclient.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Defines a named shift (e.g. "Morning", "Night", "General").
 */
@Entity
@Table(name = "shifts")
@EntityListeners(AuditingEntityListener.class)
public class Shift {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;           // e.g. "Morning Shift"

    @Column(nullable = false)
    private LocalTime startTime;   // e.g. 09:00

    @Column(nullable = false)
    private LocalTime endTime;     // e.g. 18:00

    private int gracePeriodMinutes = 15;

    // Does the shift cross midnight?
    private boolean nightShift = false;

    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE | INACTIVE

    @Column(length = 200)
    private String description;

    @CreatedDate @Column(updatable = false)
    private LocalDateTime createdAt;

    // Getters & Setters
    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }
    public String getName()                     { return name; }
    public void setName(String v)               { this.name = v; }
    public LocalTime getStartTime()             { return startTime; }
    public void setStartTime(LocalTime v)       { this.startTime = v; }
    public LocalTime getEndTime()               { return endTime; }
    public void setEndTime(LocalTime v)         { this.endTime = v; }
    public int getGracePeriodMinutes()          { return gracePeriodMinutes; }
    public void setGracePeriodMinutes(int v)    { this.gracePeriodMinutes = v; }
    public boolean isNightShift()               { return nightShift; }
    public void setNightShift(boolean v)        { this.nightShift = v; }
    public String getStatus()                   { return status; }
    public void setStatus(String v)             { this.status = v; }
    public String getDescription()              { return description; }
    public void setDescription(String v)        { this.description = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
}