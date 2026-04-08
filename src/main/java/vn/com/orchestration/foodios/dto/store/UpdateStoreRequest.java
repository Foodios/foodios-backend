package vn.com.orchestration.foodios.dto.store;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateStoreRequest extends BaseRequest {

    @Valid
    private UpdateStoreRequestData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateStoreRequestData {
        private UUID merchantId;
        @Size(max = 160)
        private String name;
        @Size(max = 180)
        private String slug;
        @Size(max = 32)
        private String phone;
        private StoreStatus status;
        @Size(max = 60)
        private String timeZone;
        @Size(max = 500)
        private String heroImageUrl;
        private LocalTime opensAt;
        private LocalTime closesAt;
        @Valid
        private StoreAddressInput address;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreAddressInput {
        @Size(max = 120)
        private String contactName;
        @Size(max = 32)
        private String contactPhone;
        @Size(max = 255)
        private String line1;
        @Size(max = 255)
        private String line2;
        @Size(max = 120)
        private String ward;
        @Size(max = 120)
        private String district;
        @Size(max = 120)
        private String city;
        @Size(max = 120)
        private String province;
        @Size(max = 20)
        private String postalCode;
        @Size(max = 2)
        private String country;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }
}
