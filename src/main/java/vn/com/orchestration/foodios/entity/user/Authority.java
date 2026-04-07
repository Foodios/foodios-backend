package vn.com.orchestration.foodios.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.entity.common.BaseEntity;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(
    name = "authorities",
    uniqueConstraints = {@UniqueConstraint(name = "uk_authorities_code", columnNames = {"code"})})
public class Authority extends BaseEntity {

  @Column(name = "code", nullable = false, length = 80)
  private String code;

  @Column(name = "name", nullable = false, length = 160)
  private String name;

  @Column(name = "description", length = 255)
  private String description;

  @Builder.Default
  @Column(name = "enabled", nullable = false)
  private boolean enabled = true;
}

