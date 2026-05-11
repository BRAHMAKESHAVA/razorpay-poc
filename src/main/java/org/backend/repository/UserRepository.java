package org.backend.repository;

import org.backend.enums.Role;
import org.backend.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByMobile(String username);
    Optional<Users> findById(Long username);
    boolean existsByMobile(String mobile);
    Optional<Users> findByMobileAndRole(String mobile, Role role);
}
