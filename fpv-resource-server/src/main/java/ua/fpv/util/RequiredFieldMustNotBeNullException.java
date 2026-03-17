package ua.fpv.util;

public class RequiredFieldMustNotBeNullException extends RuntimeException {

    public RequiredFieldMustNotBeNullException(String message) {
        super(message);
    }

}
