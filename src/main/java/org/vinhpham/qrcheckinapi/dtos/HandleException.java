package org.vinhpham.qrcheckinapi.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@Data
@EqualsAndHashCode(callSuper = true)
public class HandleException extends RuntimeException {

    private final Object[] args;
    private final HttpStatus status;

    public HandleException(String code, HttpStatus status, Object... args) {
        super(code);
        this.args = args;
        this.status = status;
    }

    public static HandleException bad(String code, Object... args) {
        return new HandleException(code, HttpStatus.BAD_REQUEST, args);
    }

}
