package edu.iiitb.esdlab3.service;

import edu.iiitb.esdlab3.dto.CreateRegistrationRequest;
import edu.iiitb.esdlab3.entity.*;
import edu.iiitb.esdlab3.exception.*;
import edu.iiitb.esdlab3.repository.EventRepository;
import edu.iiitb.esdlab3.repository.RegistrationRepository;

import java.util.List;
import java.util.Locale;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final EventService eventService;

    @Transactional
    public Registration register(
            Long eventId,
            CreateRegistrationRequest request) {

        Event event = eventService.get(eventId);

        long activeCount = registrationRepository
                .countByEvent_IdAndStatus(
                        eventId,
                        RegistrationStatus.ACTIVE
                );

        if (activeCount >= event.getCapacity()) {
            throw new EventFullException();
        }

        String email = request.getStudentEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (registrationRepository.existsByEvent_IdAndStudentEmailIgnoreCaseAndStatus(
                eventId,
                email,
                RegistrationStatus.ACTIVE)
        ) {
            throw new DuplicateRegistrationException();
        }

        return registrationRepository.save(
                new Registration(
                        event,
                        email,
                        RegistrationStatus.ACTIVE
                )
        );
    }

    public List<Registration> activeForEvent(Long eventId) {
        eventService.get(eventId);
        return registrationRepository.findActiveByEventId(eventId, RegistrationStatus.ACTIVE);
    }

    @Transactional
    public void cancel(Long registrationId) {
        Registration registration = registrationRepository
                .findById(registrationId)
                .orElseThrow(
                        () -> new RegistrationNotFoundException(registrationId)
                );

        if (registration.getStatus() == RegistrationStatus.ACTIVE) {
            registration.setStatus(RegistrationStatus.CANCELLED);
        }
    }
}