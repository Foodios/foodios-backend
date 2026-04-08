package vn.com.orchestration.foodios.entity.merchant;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Embeddable
public class MerchantPayout {
    @Column(name = "BANK_NAME")
    private String bankName;

    @Column(name = "BANK_ACCOUNT_NAME")
    private String bankAccountName;

    @Column(name = "BANK_ACCOUNT_NUMBER")
    private String bankAccountNumber;

    @Column(name = "BANK_BRANCH")
    private String bankBranch;
}
