package edu.iiitb.esdlab3.exception;

public class UnauthorizedOperationException extends RuntimeException {

    public UnauthorizedOperationException() {
        super("Provided email does not match registration email");
    }
}
