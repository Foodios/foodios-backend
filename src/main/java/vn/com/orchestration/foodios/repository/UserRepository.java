package vn.com.orchestration.foodios.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.com.orchestration.foodios.entity.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  Optional<User> findByPhone(String phone);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByPhone(String phone);

  @Query("SELECT DISTINCT u FROM User u JOIN UserRole ur ON u.id = ur.id.userId JOIN ur.role r WHERE r.code IN :roleCodes")
  Page<User> findByRoleCodes(List<String> roleCodes, Pageable pageable);

  @Query("SELECT DISTINCT u FROM User u JOIN UserRole ur ON u.id = ur.id.userId JOIN ur.role r " +
          "WHERE r.code IN :roleCodes AND (lower(u.fullName) LIKE lower(concat('%', :keyword, '%')) " +
          "OR lower(u.email) LIKE lower(concat('%', :keyword, '%')) " +
          "OR u.phone LIKE concat('%', :keyword, '%'))")
  Page<User> searchByRolesAndKeyword(List<String> roleCodes, String keyword, Pageable pageable);
}
