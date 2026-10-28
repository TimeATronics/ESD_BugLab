package edu.iiitb.esdlab3.dto;

import edu.iiitb.esdlab3.entity.Registration;
import edu.iiitb.esdlab3.entity.RegistrationStatus;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {

    private Long id;
    private Long eventId;
    private String studentEmail;
    private RegistrationStatus status;
    private boolean checkedIn;
    private LocalDateTime createdAt;

    public static RegistrationResponse from(Registration r) {
        return new RegistrationResponse(
                r.getId(),
                r.getEvent().getId(),
                r.getStudentEmail(),
                r.getStatus(),
                r.isCheckedIn(),
                r.getCreatedAt()
        );
    }
}