package vn.com.orchestration.foodios.entity.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.entity.common.BaseEntity;
import vn.com.orchestration.foodios.entity.merchant.Store;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(
    name = "categories",
    indexes = {
      @Index(name = "idx_categories_store_status_sort", columnList = "store_id,status,sort_order"),
      @Index(name = "idx_categories_store_parent", columnList = "store_id,parent_id")
    },
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_categories_store_slug", columnNames = {"store_id", "slug"})
    })
public class Category extends BaseEntity {

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Category parent;

  @Column(name = "name", nullable = false, length = 160)
  private String name;

  @Column(name = "slug", nullable = false, length = 180)
  private String slug;

  @Column(name = "description", length = 500)
  private String description;

  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Column(name = "status", nullable = false, length = 16)
  private CategoryStatus status = CategoryStatus.DRAFT;

  // Keep this flag for quick storefront filtering while status handles draft/archive lifecycle.
  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private boolean active = true;
}
