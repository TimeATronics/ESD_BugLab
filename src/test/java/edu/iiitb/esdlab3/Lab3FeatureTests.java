package edu.iiitb.esdlab3;

import edu.iiitb.esdlab3.entity.Event;
import edu.iiitb.esdlab3.entity.Registration;
import edu.iiitb.esdlab3.entity.RegistrationStatus;
import edu.iiitb.esdlab3.repository.EventRepository;
import edu.iiitb.esdlab3.repository.RegistrationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Lab3FeatureTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    EventRepository events;

    @Autowired
    RegistrationRepository registrations;

    @BeforeEach
    void clean() {
        registrations.deleteAll();
        events.deleteAll();
    }

    private Event event(String title, int capacity) {
        return events.save(new Event(title, capacity));
    }

    private Registration active(Event event, String email) {
        return registrations.save(
                new Registration(event, email, RegistrationStatus.ACTIVE)
        );
    }

    private Registration cancelled(Event event, String email) {
        return registrations.save(
                new Registration(event, email, RegistrationStatus.CANCELLED)
        );
    }

    @Test
    void transferMovesActiveRegistration() throws Exception {
        Event source = event("Source", 3);
        Event target = event("Target", 3);
        Registration registration = active(source, "student@example.com");

        mvc.perform(put("/api/registrations/" + registration.getId() + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetEventId\":" + target.getId()
                                + ",\"studentEmail\":\"STUDENT@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(target.getId().intValue()))
                .andExpect(jsonPath("$.studentEmail").value("student@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.checkedIn").value(false));

        assertEquals(
                RegistrationStatus.CANCELLED,
                registrations.findById(registration.getId()).orElseThrow().getStatus()
        );
        assertEquals(
                1L,
                registrations.countByEvent_IdAndStatus(target.getId(), RegistrationStatus.ACTIVE)
        );
    }

    @Test
    void transferRejectsWrongEmail() throws Exception {
        Event source = event("Source", 3);
        Event target = event("Target", 3);
        Registration registration = active(source, "student@example.com");

        mvc.perform(put("/api/registrations/" + registration.getId() + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetEventId\":" + target.getId()
                                + ",\"studentEmail\":\"other@example.com\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Provided email does not match registration email"));

        assertEquals(
                RegistrationStatus.ACTIVE,
                registrations.findById(registration.getId()).orElseThrow().getStatus()
        );
    }

    @Test
    void transferRejectsCancelledSource() throws Exception {
        Event source = event("Source", 3);
        Event target = event("Target", 3);
        Registration registration = cancelled(source, "student@example.com");

        mvc.perform(put("/api/registrations/" + registration.getId() + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetEventId\":" + target.getId()
                                + ",\"studentEmail\":\"student@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Registration is already cancelled"));
    }

    @Test
    void transferRejectsFullTarget() throws Exception {
        Event source = event("Source", 3);
        Event target = event("Target", 1);
        active(target, "other@example.com");
        Registration registration = active(source, "student@example.com");

        mvc.perform(put("/api/registrations/" + registration.getId() + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetEventId\":" + target.getId()
                                + ",\"studentEmail\":\"student@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Event has reached capacity"));
    }

    @Test
    void transferRejectsDuplicateOnTarget() throws Exception {
        Event source = event("Source", 3);
        Event target = event("Target", 3);
        active(target, "student@example.com");
        Registration registration = active(source, "student@example.com");

        mvc.perform(put("/api/registrations/" + registration.getId() + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetEventId\":" + target.getId()
                                + ",\"studentEmail\":\"student@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Student already has an active registration for this event"));
    }

    @Test
    void transferRejectsMissingRegistration() throws Exception {
        Event target = event("Target", 3);

        mvc.perform(put("/api/registrations/99999/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetEventId\":" + target.getId()
                                + ",\"studentEmail\":\"student@example.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void transferRejectsMissingTargetEvent() throws Exception {
        Event source = event("Source", 3);
        Registration registration = active(source, "student@example.com");

        mvc.perform(put("/api/registrations/" + registration.getId() + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetEventId\":99999"
                                + ",\"studentEmail\":\"student@example.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void transferValidatesPayload() throws Exception {
        Event source = event("Source", 3);
        Registration registration = active(source, "student@example.com");

        mvc.perform(put("/api/registrations/" + registration.getId() + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentEmail\":\"student@example.com\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/api/registrations/" + registration.getId() + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetEventId\":1,\"studentEmail\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkInMarksRegistration() throws Exception {
        Event event = event("Event", 3);
        Registration registration = active(event, "student@example.com");

        mvc.perform(post("/api/events/" + event.getId() + "/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentEmail\":\"student@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedIn").value(true));

        assertTrue(registrations.findById(registration.getId()).orElseThrow().isCheckedIn());
    }

    @Test
    void checkInRejectsUnknownEmail() throws Exception {
        Event event = event("Event", 3);

        mvc.perform(post("/api/events/" + event.getId() + "/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentEmail\":\"nobody@example.com\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Registration not found"));
    }

    @Test
    void checkInRejectsDuplicate() throws Exception {
        Event event = event("Event", 3);
        active(event, "student@example.com");

        mvc.perform(post("/api/events/" + event.getId() + "/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentEmail\":\"student@example.com\"}"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/events/" + event.getId() + "/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentEmail\":\"student@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Student has already checked in for this event"));
    }

    @Test
    void checkInRejectsCancelledRegistration() throws Exception {
        Event event = event("Event", 3);
        cancelled(event, "student@example.com");

        mvc.perform(post("/api/events/" + event.getId() + "/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentEmail\":\"student@example.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkInValidatesPayload() throws Exception {
        Event event = event("Event", 3);

        mvc.perform(post("/api/events/" + event.getId() + "/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentEmail\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkInIsCaseInsensitive() throws Exception {
        Event event = event("Event", 3);
        active(event, "student@example.com");

        mvc.perform(post("/api/events/" + event.getId() + "/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentEmail\":\"STUDENT@EXAMPLE.COM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedIn").value(true));
    }
}
