package org.saphka.locationtracker.user.exception;

public class ErrorCodeException extends RuntimeException {

    private final String code;

    public ErrorCodeException(String code, String message) {
        super(message);
        this.code = code;
    }

    ErrorCodeException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
