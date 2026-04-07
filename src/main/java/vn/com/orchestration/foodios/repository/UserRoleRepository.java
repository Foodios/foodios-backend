package vn.com.orchestration.foodios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.user.UserRole;
import vn.com.orchestration.foodios.entity.user.UserRoleId;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByIdUserId(UUID userId);
}

