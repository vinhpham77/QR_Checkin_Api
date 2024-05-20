package org.vinhpham.qrcheckinapi.dtos;

import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class RegistrationDetail {
    private Long id;
    private Date createdAt;
    private Date acceptedAt;
    private Long eventId;
    private String eventName;
    private String eventCreator;
    private String eventLocation;
    private Date checkInAt;
    private Date checkOutAt;
    private Boolean checkOutRequired;
}
