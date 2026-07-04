package com.apixenglish.center.modules.role.repository;

import com.apixenglish.center.modules.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    Optional<Role> findByCodeAndDeletedAtIsNull(String code);
}
