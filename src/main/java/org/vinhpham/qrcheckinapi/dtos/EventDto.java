package org.vinhpham.qrcheckinapi.dtos;

import jakarta.validation.constraints.*;
import lombok.*;
import org.vinhpham.qrcheckinapi.entities.Category;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * DTO for {@link org.vinhpham.qrcheckinapi.entities.Event}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto implements Serializable {
    Long id;

    @NotBlank(message = "{event.name.blank}")
    @Size(max = 100, message = "{event.name.size}")
    String name;

    @Size(max = 3, message = "{event.categories.size}")
    Set<Category> categories;

    String description;

    @Negative(message = "{event.slots.negative}")
    Integer slots;

    @NotNull(message = "{event.startAt.blank}")
    Date startAt;

    @NotNull(message = "{event.endAt.blank}")
    Date endAt;

    @NotBlank(message = "{event.location.blank}")
    @Size(max = 255, message = "{event.location.size}")
    String location;

    BigDecimal latitude;
    BigDecimal longitude;

    @NotNull(message = "{event.radius.blank}")
    @Positive(message = "{event.radius.positive}")
    Double radius;

    Boolean isTicketSeller = false;

    Boolean regisRequired = false;

    Boolean approvalRequired = false;

    Boolean captureRequired = false;

    String checkoutQrCode = null;

    @Size(max = 255, message = "{event.backgroundUrl.size}")
    String backgroundImage;

    String checkinQrCode;

    double distance;

    String createdBy;

    Date createdAt;

    String updatedBy;

    Date updatedAt;

    List<TicketTypeDto> ticketTypes;

    Boolean isRegistered;

    @AssertTrue(message = "{event.endAt.after.startAt}")
    private boolean isEndAtAfterStartAt() {
        if (startAt == null || endAt == null) {
            return true;
        }
        return endAt.after(startAt);
    }
}
