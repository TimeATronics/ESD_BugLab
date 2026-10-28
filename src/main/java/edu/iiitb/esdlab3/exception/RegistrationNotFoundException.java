package edu.iiitb.esdlab3.exception;

public class RegistrationNotFoundException extends RuntimeException {

    public RegistrationNotFoundException() {
        super("Registration not found");
    }

    public RegistrationNotFoundException(Long id) {
        super("Registration not found: " + id);
    }
}