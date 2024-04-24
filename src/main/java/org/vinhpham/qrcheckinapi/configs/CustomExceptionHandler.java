package org.vinhpham.qrcheckinapi.configs;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.vinhpham.qrcheckinapi.common.Utils;
import org.vinhpham.qrcheckinapi.dtos.Failure;
import org.vinhpham.qrcheckinapi.dtos.HandleException;
import org.vinhpham.qrcheckinapi.services.ImageService;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class CustomExceptionHandler {

    private final MessageSource messageSource;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    @ExceptionHandler({HandleException.class})
    public ResponseEntity<?> handleException(final HandleException ex) {
        String message = Utils.getMessage(messageSource, ex.getMessage(), ex.getArgs());
        return Failure.response(message, ex.getStatus());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<?> handleUnexpectedException(final Exception throwable) {
        LOGGER.error("Unexpected error", throwable);
        return Failure.internal(messageSource, "error.something.wrong");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String errorMessage = error.getDefaultMessage();
            errors.add(errorMessage);
        });

        String message = String.join("\n", errors);
        return Failure.bad(message);
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<?> handleNoHandlerFoundException(final HttpRequestMethodNotSupportedException ex) {
        LOGGER.error("Method not allowed", ex);
        return Failure.response(messageSource, "error.not.found", HttpStatus.NOT_FOUND);
    }
}
