package vn.com.orchestration.foodios.constant;

public final class ApiConstant {

  private ApiConstant() {}

  public static final String API_PATH = "/api";
  public static final String API_VERSION = "/v1";
  public static final String API_PREFIX = API_PATH + API_VERSION; // /api/v1

  public static final String ADMIN_PATH = "/admin";
  public static final String USERS_PATH = "/users";
  public static final String ROLES_PATH = "/roles";
  public static final String AUTHORITIES_PATH = "/authorities";
  public static final String AUTHENTICATION_PATH = "/authentication";
  public static final String MERCHANT_PATH = "/merchant";
  public static final String MEDIA_PATH = "/media";
  public static final String MERCHANTS_PATH = "/merchants";
  public static final String PRODUCTS_PATH = "/products";
  public static final String REGISTER_PATH = "/register";
  public static final String LOGIN_PATH = "/login";
  public static final String VERIFY_EMAIL_PATH = "/verify-email";
  public static final String REFRESH_TOKEN_PATH = "/refresh-token";
  public static final String LOGOUT_PATH = "/logout";
  public static final String UPLOAD_PATH = "/upload";
}
