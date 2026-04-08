package vn.com.orchestration.foodios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.user.Authority;

import java.util.Optional;
import java.util.UUID;

public interface AuthorityRepository extends JpaRepository<Authority, UUID> {
    Optional<Authority> findByCode(String code);

    boolean existsByCode(String code);
}
