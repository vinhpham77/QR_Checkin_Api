package org.vinhpham.qrcheckinapi.dtos;

import lombok.Data;

@Data
public class GenerateQrRequest {
    private Long eventId;
    private boolean isCheckIn;
}
