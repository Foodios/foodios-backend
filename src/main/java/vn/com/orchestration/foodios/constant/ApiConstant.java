package vn.com.orchestration.foodios.constant;

public final class ApiConstant {

  private ApiConstant() {}

  public static final String API_PATH = "/api";
  public static final String API_VERSION = "/v1";
  public static final String API_PREFIX = API_PATH + API_VERSION; // /api/v1

  public static final String AUTHENTICATION_PATH = "/authentication";
  public static final String REGISTER_PATH = "/register";
  public static final String LOGIN_PATH = "/login";
  public static final String VERIFY_EMAIL_PATH = "/verify-email";
}
