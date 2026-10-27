# Feature Implementation Guide

This guide outlines two feature implementation tasks. For each feature, follow the structured steps across DTO, Entity/Repository, Service, Controller, and Custom Exception Handling.

---

## Feature 1: Registration Transfer Between Events

### Task Description
Allow a student with an active registration for an event to transfer their seat to another event. The transfer must verify student ownership, check target event validity and capacity, and execute atomically.

### Implementation Steps

#### 1. DTO Layer
* Create `TransferRegistrationRequest` in `edu.iiitb.esdlab3.dto`:
  * `targetEventId` (`Long`, must have `@NotNull(message = "Target event ID is required")`)
  * `studentEmail` (`String`, must have `@NotBlank(message = "Student email is required")` and `@Email(message = "Invalid email format")`)

#### 2. Custom Exception Layer
* Create `UnauthorizedOperationException` in `edu.iiitb.esdlab3.exception`:
  * Extends `RuntimeException`.
  * Message: `"Provided email does not match registration email"`.

#### 3. Service Layer (`RegistrationService`)
* Add method `@Transactional public Registration transfer(Long registrationId, TransferRegistrationRequest request)`:
  1. **Fetch current registration:** Find by `registrationId`; throw `RegistrationNotFoundException` if missing.
  2. **Verify active status:** If status is `CANCELLED`, throw `RegistrationConflictException`.
  3. **Verify ownership:** If `studentEmail` does not match `registration.getStudentEmail()` (case-insensitive), throw `UnauthorizedOperationException`.
  4. **Validate target event:** Call `eventService.get(request.getTargetEventId())` (throws `EventNotFoundException` if not found).
  5. **Check target capacity:** Count active registrations on target event; throw `EventFullException` if active count $\ge$ target capacity.
  6. **Check duplicate on target:** Verify student has no existing active registration on target event; throw `DuplicateRegistrationException` if duplicate exists.
  7. **Atomic transfer:** Mark current registration as `CANCELLED`, create and persist a new active `Registration` for the target event, and return it.

#### 4. Controller Layer (`RegistrationController`)
* Add endpoint:
  * `PUT /api/registrations/{registrationId}/transfer`
  * Parameters: `@PathVariable Long registrationId`, `@Valid @RequestBody TransferRegistrationRequest request`
  * Response: HTTP `200 OK` returning `RegistrationResponse.from(transferredRegistration)`

#### 5. Exception Handler Layer (`ApiExceptionHandler`)
* Add handler method:
  * Catch: `UnauthorizedOperationException.class`
  * Return: `HttpStatus.FORBIDDEN` (`403`) with structured `ApiError` body.

---

## Feature 2: Event Check-In / Attendance Verification

### Task Description
Allow event organizers to check in students at the venue. The system must verify that the student holds an active registration for the event and prevent duplicate check-ins.

### Implementation Steps

#### 1. Entity & Repository Layer
* **Entity (`Registration`):**
  * Add field: `private boolean checkedIn = false;` (with getter).
  * Add method: `public void markCheckedIn() { this.checkedIn = true; }`.
* **Repository (`RegistrationRepository`):**
  * Add query method:
    `Optional<Registration> findByEvent_IdAndStudentEmailIgnoreCaseAndStatus(Long eventId, String studentEmail, RegistrationStatus status);`

#### 2. DTO Layer
* Create `CheckInRequest` in `edu.iiitb.esdlab3.dto`:
  * `studentEmail` (`String`, must have `@NotBlank(message = "Student email is required")` and `@Email(message = "Invalid email format")`)
* Update `RegistrationResponse` in `edu.iiitb.esdlab3.dto`:
  * Add `boolean checkedIn` field to include check-in status in the API response.

#### 3. Custom Exception Layer
* Create `AlreadyCheckedInException` in `edu.iiitb.esdlab3.exception`:
  * Extends `RuntimeException`.
  * Message: `"Student has already checked in for this event"`.

#### 4. Service Layer (`RegistrationService`)
* Add method `@Transactional public Registration checkIn(Long eventId, CheckInRequest request)`:
  1. **Verify event:** Call `eventService.get(eventId)` (throws `EventNotFoundException` if not found).
  2. **Find active registration:** Query `registrationRepository.findByEvent_IdAndStudentEmailIgnoreCaseAndStatus(eventId, request.getStudentEmail(), RegistrationStatus.ACTIVE)`.
     * Throw `RegistrationNotFoundException` if no active registration is found.
  3. **Prevent duplicate check-in:** If `registration.isCheckedIn()` is `true`, throw `AlreadyCheckedInException`.
  4. **Update check-in state:** Call `registration.markCheckedIn()`, save to repository, and return the registration.

#### 5. Controller Layer (`RegistrationController`)
* Add endpoint:
  * `POST /api/events/{eventId}/check-in`
  * Parameters: `@PathVariable Long eventId`, `@Valid @RequestBody CheckInRequest request`
  * Response: HTTP `200 OK` returning `RegistrationResponse.from(registration)`

#### 6. Exception Handler Layer (`ApiExceptionHandler`)
* Add handler method:
  * Catch: `AlreadyCheckedInException.class`
  * Return: `HttpStatus.CONFLICT` (`409`) with structured `ApiError` body.

---

## Expected Verification Matrix

| Feature | Scenario | Input / Action | Expected HTTP Status | Expected Response Body |
|---|---|---|---|---|
| **Transfer** | Valid transfer | Active registration + available target event | `200 OK` | `RegistrationResponse` with target event |
| **Transfer** | Wrong student email | Email does not match original registration | `403 Forbidden` | `ApiError` (`Provided email does not match...`) |
| **Transfer** | Target event full | Target event active count == capacity | `409 Conflict` | `ApiError` (`Event has reached capacity`) |
| **Transfer** | Target duplicate | Student already registered in target event | `409 Conflict` | `ApiError` (`Student already has active...`) |
| **Check-In** | Valid check-in | Active registered student | `200 OK` | `RegistrationResponse` with `checkedIn: true` |
| **Check-In** | Unregistered student | Email not registered / cancelled | `404 Not Found` | `ApiError` (`Registration not found`) |
| **Check-In** | Duplicate check-in | Student already marked checked-in | `409 Conflict` | `ApiError` (`Student has already checked in...`) |
| **Both** | Invalid payload | Blank/malformed email | `400 Bad Request` | `ApiError` validation details |
