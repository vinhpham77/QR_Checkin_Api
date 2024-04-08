package org.vinhpham.qrcheckinapi.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
public class HandleException extends RuntimeException {

    private final HttpStatus status;

    public HandleException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static HandleException bad(String message) {
        return new HandleException(message, HttpStatus.BAD_REQUEST);
    }

}
