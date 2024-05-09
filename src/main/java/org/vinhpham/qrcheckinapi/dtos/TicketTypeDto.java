package org.vinhpham.qrcheckinapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypeDto {
    Long id;
    String name;
    String description;
    Double price;
    Integer quantity;
}
