package vn.com.orchestration.foodios.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_MESSAGE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResult {
  private String responseCode;
  private String description;
}

