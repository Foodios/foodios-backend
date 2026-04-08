package vn.com.orchestration.foodios.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.entity.common.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(
    name = "app_users",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_app_users_username", columnNames = {"username"}),
      @UniqueConstraint(name = "uk_app_users_email", columnNames = {"email"}),
      @UniqueConstraint(name = "uk_app_users_phone", columnNames = {"phone"})
    })
public class User extends BaseEntity {

  @Column(name = "username", nullable = false, length = 60)
  private String username;

  @Column(name = "email", nullable = false, length = 254)
  private String email;

  @Column(name = "phone", length = 32)
  private String phone;

  @ToString.Exclude
  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "full_name", length = 160)
  private String fullName;

  @Builder.Default
  @Column(name = "profile_completed", nullable = false)
  private boolean profileCompleted = false;

  @Column(name = "AVATAR_URL")
  private String avatarUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  @Builder.Default
  private UserStatus status = UserStatus.ACTIVE;
}
