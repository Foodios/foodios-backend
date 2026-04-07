package vn.com.orchestration.foodios.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import static vn.com.orchestration.foodios.constant.RegexConstant.CHANNEL_REGEX;
import static vn.com.orchestration.foodios.constant.RegexConstant.DATETIME_REGEX;
import static vn.com.orchestration.foodios.constant.RegexConstant.REQUEST_ID_REGEX;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class BaseRequest {

  @NotBlank
  @Pattern(regexp = REQUEST_ID_REGEX, message = "RequestId must be in xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
  private String requestId;

  @NotBlank
  @Pattern(regexp = DATETIME_REGEX, message = "RequestDateTime must be in yyyy-MM-dd'T'HH:mm:ss.SSS format (e.g: 2025-11-01T16:27:00.135+7:00)")
  private String requestDateTime;

  @NotBlank
  @Size(max = 30, message = "Channel can't exceed 30 characters")
  @Pattern(regexp = CHANNEL_REGEX, message = "Channel must be ONL, OFF")
  private String channel;
}


