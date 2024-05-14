package org.vinhpham.qrcheckinapi.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketCheckinRequest {
    private String code;
    private Long eventId;
}
