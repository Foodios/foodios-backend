package vn.com.orchestration.foodios.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreAddressPayload {
    private String contactName;
    private String contactPhone;
    private String line1;
    private String line2;
    private String ward;
    private String district;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
