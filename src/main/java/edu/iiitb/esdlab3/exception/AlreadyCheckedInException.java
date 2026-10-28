package edu.iiitb.esdlab3.exception;

public class AlreadyCheckedInException extends RuntimeException {

    public AlreadyCheckedInException() {
        super("Student has already checked in for this event");
    }
}
