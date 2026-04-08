package vn.com.orchestration.foodios.dto.storefront;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.orchestration.foodios.dto.store.StoreAddressPayload;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorefrontStoreLocationPayload {
    private UUID id;
    private String name;
    private String phone;
    private String timeZone;
    private String heroImageUrl;
    private LocalTime opensAt;
    private LocalTime closesAt;
    private StoreAddressPayload address;
}
