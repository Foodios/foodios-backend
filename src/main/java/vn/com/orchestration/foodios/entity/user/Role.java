package vn.com.orchestration.foodios.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.entity.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(
    name = "roles",
    uniqueConstraints = {@UniqueConstraint(name = "uk_roles_code", columnNames = {"code"})})
public class Role extends BaseEntity {

  @Column(name = "code", nullable = false, length = 64)
  private String code;

  @Column(name = "name", nullable = false, length = 120)
  private String name;

  @Column(name = "description", length = 255)
  private String description;

  @Builder.Default
  @Column(name = "enabled", nullable = false)
  private boolean enabled = true;

  @Builder.Default
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "role_authorities",
      joinColumns = @JoinColumn(name = "role_id", nullable = false),
      inverseJoinColumns = @JoinColumn(name = "authority_id", nullable = false))
  private Set<Authority> authorities = new HashSet<>();
}
