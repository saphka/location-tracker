package org.saphka.locationtracker.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
public class ErrorHandler extends DefaultErrorAttributes {

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";

    private static final Map<String, HttpStatus> statusMap = Map.ofEntries(
            Map.entry(USER_NOT_FOUND, HttpStatus.NOT_FOUND)
    );

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);

        Throwable error = getError(request);

        if (error instanceof ErrorCodeException) {
            ErrorCodeException errorCodeException = (ErrorCodeException) error;

            errorAttributes.put("code", errorCodeException.getCode());
        } else {
            errorAttributes.put("code", "INTERNAL_ERROR");
        }

        return errorAttributes;
    }
}
