package ua.fpv.util;

public class UsernameIsAlreadyExistsException extends RuntimeException {

    public UsernameIsAlreadyExistsException(String message) {
        super(message);
    }
}
