package org.saphka.locationtracker.user.controller;

import org.saphka.locationtracker.user.api.model.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String PASSWORD_MISMATCH = "PASSWORD_MISMATCH";

    private static final Map<String, HttpStatus> statusMap = Map.ofEntries(
            Map.entry(USER_NOT_FOUND, HttpStatus.NOT_FOUND),
            Map.entry(PASSWORD_MISMATCH, HttpStatus.UNAUTHORIZED)
    );

    private ResponseEntity<ErrorDTO> buildWithCode(String code, String message) {
        ErrorDTO body = new ErrorDTO();
        body.setCode(code);
        body.setMessage(message);

        return new ResponseEntity<>(body, statusMap.getOrDefault(code, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
