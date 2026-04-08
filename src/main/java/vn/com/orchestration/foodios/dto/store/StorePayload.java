package vn.com.orchestration.foodios.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorePayload {
    private UUID id;
    private UUID merchantId;
    private String name;
    private String slug;
    private String phone;
    private StoreStatus status;
    private String timeZone;
    private String heroImageUrl;
    private LocalTime opensAt;
    private LocalTime closesAt;
    private StoreAddressPayload address;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
