package edu.iiitb.esdlab3.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Table(name = "event_registrations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String studentEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    @Column(nullable = false)
    private boolean checkedIn = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Registration(
            Event event,
            String studentEmail,
            RegistrationStatus status) {

        this.event = event;
        this.studentEmail = studentEmail;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public void markCheckedIn() {
        this.checkedIn = true;
    }
}