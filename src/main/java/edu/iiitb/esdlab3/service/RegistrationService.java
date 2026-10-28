package edu.iiitb.esdlab3.service;

import edu.iiitb.esdlab3.dto.CheckInRequest;
import edu.iiitb.esdlab3.dto.CreateRegistrationRequest;
import edu.iiitb.esdlab3.dto.TransferRegistrationRequest;
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

    @Transactional
    public Registration transfer(
            Long registrationId,
            TransferRegistrationRequest request) {

        Registration registration = registrationRepository
                .findById(registrationId)
                .orElseThrow(
                        () -> new RegistrationNotFoundException(registrationId)
                );

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new RegistrationConflictException();
        }

        if (!registration.getStudentEmail()
                .equalsIgnoreCase(request.getStudentEmail())) {
            throw new UnauthorizedOperationException();
        }

        Event targetEvent = eventService.get(request.getTargetEventId());

        long activeCount = registrationRepository
                .countByEvent_IdAndStatus(
                        targetEvent.getId(),
                        RegistrationStatus.ACTIVE
                );

        if (activeCount >= targetEvent.getCapacity()) {
            throw new EventFullException();
        }

        if (registrationRepository
                .existsByEvent_IdAndStudentEmailIgnoreCaseAndStatus(
                        targetEvent.getId(),
                        registration.getStudentEmail(),
                        RegistrationStatus.ACTIVE)) {
            throw new DuplicateRegistrationException();
        }

        registration.setStatus(RegistrationStatus.CANCELLED);

        return registrationRepository.save(
                new Registration(
                        targetEvent,
                        registration.getStudentEmail(),
                        RegistrationStatus.ACTIVE
                )
        );
    }

    @Transactional
    public Registration checkIn(
            Long eventId,
            CheckInRequest request) {

        eventService.get(eventId);

        Registration registration = registrationRepository
                .findByEvent_IdAndStudentEmailIgnoreCaseAndStatus(
                        eventId,
                        request.getStudentEmail(),
                        RegistrationStatus.ACTIVE
                )
                .orElseThrow(RegistrationNotFoundException::new);

        if (registration.isCheckedIn()) {
            throw new AlreadyCheckedInException();
        }

        registration.markCheckedIn();

        return registrationRepository.save(registration);
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