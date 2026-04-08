package vn.com.orchestration.foodios.entity.review;

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
import vn.com.orchestration.foodios.entity.order.FoodOrder;
import vn.com.orchestration.foodios.entity.user.User;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(
    name = "reviews",
    indexes = {
      @Index(name = "idx_reviews_store_status_created", columnList = "store_id,status,created_at"),
      @Index(name = "idx_reviews_order_status_created", columnList = "order_id,status,created_at")
    },
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_reviews_order", columnNames = {"order_id"})
    })
public class Review extends BaseEntity {

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private FoodOrder order;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id", nullable = false)
  private User customer;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 16)
  private ReviewSourceType sourceType;

  @Column(name = "rating", nullable = false)
  private Integer rating;

  @Column(name = "title", length = 160)
  private String title;

  @Column(name = "comment", length = 1000)
  private String comment;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Column(name = "status", nullable = false, length = 16)
  private ReviewStatus status = ReviewStatus.PUBLISHED;

  @Builder.Default
  @Column(name = "reviewed_at", nullable = false)
  private Instant reviewedAt = Instant.now();
}
