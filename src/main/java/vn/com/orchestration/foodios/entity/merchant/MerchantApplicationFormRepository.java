package vn.com.orchestration.foodios.entity.merchant;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MerchantApplicationFormRepository extends JpaRepository<MerchantApplicationForm, UUID> {
    boolean existsByLegalNameAndStatusIn(String legalName, List<ApplicationFormStatus> submitted);

    boolean existsBySlugIgnoreCaseAndStatusIn(String slug, List<ApplicationFormStatus> submitted);

    Page<MerchantApplicationForm> findAllByStatus(ApplicationFormStatus status, Pageable pageable);

    @Query(value = """
            SELECT generate_merchant_application_form_code()
            """, nativeQuery = true)
    String generateFormCode();
}
