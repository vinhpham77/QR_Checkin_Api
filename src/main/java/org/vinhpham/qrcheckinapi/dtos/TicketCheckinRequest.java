package org.vinhpham.qrcheckinapi.dtos;

import lombok.Data;

@Data
public class TicketCheckinRequest {
    String code;
    Long eventId;
}
