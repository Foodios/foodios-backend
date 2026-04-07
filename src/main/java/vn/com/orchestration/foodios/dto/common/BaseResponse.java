package vn.com.orchestration.foodios.dto.common;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseResponse<T> {
  private String requestId;
  private String requestDateTime;
  private String channel;
  private ApiResult result;
  private T data;
}
