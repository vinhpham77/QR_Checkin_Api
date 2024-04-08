package org.vinhpham.qrcheckinapi.dtos;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.vinhpham.qrcheckinapi.common.Utils;

public class Success {

    public static ResponseEntity<?> noContent() {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public static ResponseEntity<?> ok(Object data) {
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    public static ResponseEntity<?> response(Object data, HttpStatus status) {
        return new ResponseEntity<>(data, status);
    }

    public static ResponseEntity<?> response(MessageSource messageSource, String code, HttpStatus status, Object... args) {
        String message = Utils.getMessage(messageSource, code, args);
        return new ResponseEntity<>(message, status);
    }
}
