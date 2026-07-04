package com.apixenglish.center.modules.user.repository;

import com.apixenglish.center.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    
    boolean existsByEmailAndDeletedAtIsNull(String email);
    
    boolean existsByPhoneAndDeletedAtIsNull(String phone);
}
