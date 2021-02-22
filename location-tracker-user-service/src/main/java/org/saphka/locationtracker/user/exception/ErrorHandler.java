package org.saphka.locationtracker.user.exception;

import org.saphka.locationtracker.user.api.model.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";

    private static final Map<String, HttpStatus> statusMap = Map.ofEntries(
            Map.entry(USER_NOT_FOUND, HttpStatus.NOT_FOUND)
    );

    @ExceptionHandler(ErrorCodeException.class)
    public ResponseEntity<Object> handleNodataFoundException(ErrorCodeException ex, WebRequest request) {

        ErrorDTO body = new ErrorDTO();
        String code = ex.getCode();
        body.setCode(code);
        body.setMessage(ex.getMessage());

        return new ResponseEntity<>(body, statusMap.getOrDefault(code, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
