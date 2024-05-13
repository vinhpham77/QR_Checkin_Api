package org.vinhpham.qrcheckinapi.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AttendanceRequest {
    private String code;
    private Long eventId;

    public String getCode() {
        return code;
    }

    public Long getEventId() {
        return eventId;
    }
}
