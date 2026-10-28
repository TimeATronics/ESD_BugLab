package edu.iiitb.esdlab3.controller;

import edu.iiitb.esdlab3.dto.*;
import edu.iiitb.esdlab3.service.RegistrationService;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/api/events/{eventId}/registrations")
    public ResponseEntity<RegistrationResponse> register(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateRegistrationRequest request) {

        var r = registrationService.register(eventId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(r.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(RegistrationResponse.from(r));
    }

    @PutMapping("/api/registrations/{registrationId}/transfer")
    public ResponseEntity<RegistrationResponse> transfer(
            @PathVariable Long registrationId,
            @Valid @RequestBody TransferRegistrationRequest request) {

        var r = registrationService.transfer(registrationId, request);

        return ResponseEntity.ok(RegistrationResponse.from(r));
    }

    @PostMapping("/api/events/{eventId}/check-in")
    public ResponseEntity<RegistrationResponse> checkIn(
            @PathVariable Long eventId,
            @Valid @RequestBody CheckInRequest request) {

        var r = registrationService.checkIn(eventId, request);

        return ResponseEntity.ok(RegistrationResponse.from(r));
    }

    @GetMapping("/api/events/{eventId}/registrations")
    public List<RegistrationResponse> list(@PathVariable Long eventId) {
        return registrationService.activeForEvent(eventId)
                .stream()
                .map(RegistrationResponse::from)
                .toList();
    }

    @DeleteMapping("/api/registrations/{registrationId}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long registrationId) {

        registrationService.cancel(registrationId);

        return ResponseEntity.noContent().build();
    }
}