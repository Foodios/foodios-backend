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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.entity.common.BaseEntity;
import vn.com.orchestration.foodios.entity.merchant.Store;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(
    name = "products",
    indexes = {
      @Index(name = "idx_products_store_status_sort", columnList = "store_id,status,sort_order"),
      @Index(name = "idx_products_store_category_status", columnList = "store_id,category_id,status"),
      @Index(name = "idx_products_store_featured", columnList = "store_id,is_featured")
    },
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_products_store_slug", columnNames = {"store_id", "slug"}),
      @UniqueConstraint(name = "uk_products_store_sku", columnNames = {"store_id", "sku"})
    })
public class Product extends BaseEntity {

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @Column(name = "name", nullable = false, length = 200)
  private String name;

  @Column(name = "slug", nullable = false, length = 220)
  private String slug;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(name = "price", nullable = false, precision = 19, scale = 2)
  private BigDecimal price;

  @Column(name = "compare_at_price", precision = 19, scale = 2)
  private BigDecimal compareAtPrice;

  @Builder.Default
  @Column(name = "currency", nullable = false, length = 3)
  private String currency = "VND";

  @Column(name = "sku", length = 64)
  private String sku;

  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @Column(name = "stock_quantity", nullable = false)
  @Builder.Default
  private int stockQuantity = 0;

  @Column(name = "sort_order", nullable = false)
  @Builder.Default
  private int sortOrder = 0;

  @Column(name = "is_featured", nullable = false)
  @Builder.Default
  private boolean featured = false;

  @Column(name = "is_available", nullable = false)
  @Builder.Default
  private boolean available = true;

  @Column(name = "preparation_time_minutes")
  private Integer preparationTimeMinutes;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Column(name = "status", nullable = false, length = 16)
  private ProductStatus status = ProductStatus.DRAFT;
}
