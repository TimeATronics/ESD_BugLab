package edu.iiitb.esdlab3.repository;

import edu.iiitb.esdlab3.entity.Registration;
import edu.iiitb.esdlab3.entity.RegistrationStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistrationRepository
        extends JpaRepository<Registration, Long> {

    long countByEvent_IdAndStatus(
            Long eventId,
            RegistrationStatus status
    );

    boolean existsByEvent_IdAndStudentEmailIgnoreCaseAndStatus(
            Long eventId,
            String studentEmail,
            RegistrationStatus status
    );

    @Query("SELECT r FROM Registration r WHERE r.event.id = :eventId AND r.status = :status")
    List<Registration> findActiveByEventId(
            @Param("eventId") Long eventId,
            @Param("status") RegistrationStatus status
    );
}