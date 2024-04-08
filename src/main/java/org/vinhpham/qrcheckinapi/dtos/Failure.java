package org.vinhpham.qrcheckinapi.dtos;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.vinhpham.qrcheckinapi.common.Utils;

public class Failure {

    public static ResponseEntity<Message> response(String message, HttpStatus status) {
        return new ResponseEntity<>(new Message(message), status);
    }

    public static ResponseEntity<Message> bad(String message) {
        return new ResponseEntity<>(new Message(message), HttpStatus.BAD_REQUEST);
    }

    public static ResponseEntity<Message> internal(MessageSource messageSource, String code, Object... args) {
        String message = Utils.getMessage(messageSource, code, args);
        return new ResponseEntity<>(new Message(message), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ResponseEntity<Message> response(MessageSource messageSource, String code, HttpStatus status, Object... args) {
        String message = Utils.getMessage(messageSource, code, args);
        return new ResponseEntity<>(new Message(message), status);
    }

}
