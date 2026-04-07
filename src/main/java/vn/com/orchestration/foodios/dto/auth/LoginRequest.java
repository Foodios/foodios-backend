package vn.com.orchestration.foodios.dto.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LoginRequest extends BaseRequest {

  @Valid
  @NotNull
  private LoginRequestData data;

  @Getter
  @Setter
  public static class LoginRequestData {
    @NotBlank
    @Size(max = 254)
    private String identifier; // username | email | phone

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;
  }
}
