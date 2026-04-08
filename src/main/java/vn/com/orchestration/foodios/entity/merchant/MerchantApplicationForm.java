package vn.com.orchestration.foodios.entity.merchant;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.entity.common.BaseEntity;

import java.time.OffsetDateTime;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "merchant_application_forms")
public class MerchantApplicationForm extends BaseEntity {

    @Column(name = "formCode")
    private String formCode;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "submitted_by", nullable = false)
    private String submittedBy;

    @Column(name = "LEGAL_NAME", nullable = false)
    private String legalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationFormStatus status;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    // ===== owner snapshot =====
    @Column(name = "owner_full_name")
    private String ownerFullName;

    @Column(name = "owner_email")
    private String ownerEmail;

    @Column(name = "owner_phone")
    private String ownerPhone;

    // ===== merchant info =====
    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "description")
    private String description;

    @Column(name = "tax_code")
    private String taxCode;

    @Column(name = "business_registration_number")
    private String businessRegistrationNumber;

    @Column(name = "business_license_image_url")
    private String businessLicenseImageUrl;

    @Column(name = "food_safety_license_image_url")
    private String foodSafetyLicenseImageUrl;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_name")
    private String contactName;

    // ===== address snapshot =====
    @Column(name = "line1")
    private String line1;

    @Column(name = "line2")
    private String line2;

    @Column(name = "district")
    private String district;

    @Column(name = "city")
    private String city;

    @Column(name = "province")
    private String province;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    private String country;

    // ===== payout =====
    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_name")
    private String bankAccountName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_branch")
    private String bankBranch;
}