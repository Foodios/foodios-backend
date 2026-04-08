package vn.com.orchestration.foodios.dto.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
public class RegisterRequest extends BaseRequest {

  @Valid
  @NotNull
  private RegisterRequestData data;

  @Getter
  @Setter
  public static class RegisterRequestData {
    @NotBlank
    @NotNull
    @Size(min = 3, max = 60)
    private String username;

    @NotBlank
    @NotNull
    @Size(min = 8, max = 72)
    private String password;

    @NotBlank
    @Email
    @NotNull
    @Size(max = 254)
    private String email;

    @NotBlank
    @NotNull
    @Size(min = 8, max = 32)
    private String phone;

    @NotBlank
    @NotNull
    private String fullName;
  }
}
