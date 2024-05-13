package org.vinhpham.qrcheckinapi.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TicketDetail {
    private Long id;
    private String ticketTypeName;
    private String qrCode;
    private String username;
    private Date createdAt;
    private Date checkInAt;
    private Double price;
    private Long eventId;
    private String eventName;
    private String location;
}
