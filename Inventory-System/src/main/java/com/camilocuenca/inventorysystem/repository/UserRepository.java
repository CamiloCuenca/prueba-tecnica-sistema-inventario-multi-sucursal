package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.User;
import com.camilocuenca.inventorysystem.Enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByBranchIdAndRoleIn(UUID branchId, List<Role> roles);
}
