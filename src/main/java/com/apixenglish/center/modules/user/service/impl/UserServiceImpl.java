package com.apixenglish.center.modules.user.service.impl;

import com.apixenglish.center.common.exception.ConflictException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.exception.BusinessException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.audit.service.AuditService;
import com.apixenglish.center.modules.campus.entity.Campus;
import com.apixenglish.center.modules.campus.repository.CampusRepository;
import com.apixenglish.center.modules.role.entity.Role;
import com.apixenglish.center.modules.role.repository.RoleRepository;
import com.apixenglish.center.modules.user.dto.request.CreateUserRequest;
import com.apixenglish.center.modules.user.dto.request.UpdateUserRequest;
import com.apixenglish.center.modules.user.dto.request.AssignRolesRequest;
import com.apixenglish.center.modules.user.dto.request.UpdateRoleAssignmentRequest;
import com.apixenglish.center.modules.user.dto.request.ResetPasswordRequest;
import com.apixenglish.center.modules.user.dto.response.UserResponse;
import com.apixenglish.center.modules.user.dto.response.UserRoleAssignmentResponse;
import com.apixenglish.center.modules.user.dto.response.UserLookupResponse;
import com.apixenglish.center.modules.user.entity.User;
import com.apixenglish.center.modules.user.entity.UserRole;
import com.apixenglish.center.modules.user.entity.UserStatus;
import com.apixenglish.center.modules.user.mapper.UserMapper;
import com.apixenglish.center.modules.user.repository.UserRepository;
import com.apixenglish.center.modules.user.repository.UserRoleRepository;
import com.apixenglish.center.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final CampusRepository campusRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(String search, UserStatus status, UUID roleId, UUID campusId, Pageable pageable) {
        Page<User> page = userRepository.searchUsers(search, status, roleId, campusId, pageable);
        return PageResponse.of(page.map(user -> {
            List<UserRole> userRoles = userRoleRepository.findByUserIdAndDeletedAtIsNull(user.getId());
            return userMapper.toResponse(user, userRoles);
        }));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndDeletedAtIsNull(id);
        return userMapper.toResponse(user, userRoles);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Validation: at least one of email, phone, or username is required
        if (isBlank(request.getEmail()) && isBlank(request.getPhone()) && isBlank(request.getUsername())) {
            throw new BusinessException("At least one of email, phone, or username is required", "CONTACT_REQUIRED");
        }

        // Email uniqueness validation
        if (!isBlank(request.getEmail())) {
            if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
                throw new ConflictException("Email already exists", "DUPLICATE_EMAIL");
            }
        }

        // Phone uniqueness validation
        if (!isBlank(request.getPhone())) {
            if (userRepository.existsByPhoneAndDeletedAtIsNull(request.getPhone())) {
                throw new ConflictException("Phone already exists", "DUPLICATE_PHONE");
            }
        }

        // Username uniqueness validation
        if (!isBlank(request.getUsername())) {
            if (userRepository.existsByUsernameAndDeletedAtIsNull(request.getUsername())) {
                throw new ConflictException("Username already exists", "DUPLICATE_USERNAME");
            }
        }

        // Password required check
        String rawPassword = request.getPassword();
        if (isBlank(rawPassword)) {
            rawPassword = request.getTemporaryPassword();
        }
        if (isBlank(rawPassword)) {
            throw new BusinessException("Password or temporary password is required on creation", "PASSWORD_REQUIRED");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
                .emailVerified(false)
                .phoneVerified(false)
                .mfaEnabled(false)
                .build();

        User saved = userRepository.save(user);
        recordAudit("USER_CREATED", saved, null);
        return userMapper.toResponse(saved, List.of());
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (isBlank(request.getEmail()) && isBlank(request.getPhone()) && isBlank(request.getUsername())) {
            throw new BusinessException("At least one of email, phone, or username is required", "CONTACT_REQUIRED");
        }

        if (!isBlank(request.getEmail())) {
            if (userRepository.existsByEmailAndIdNotAndDeletedAtIsNull(request.getEmail(), id)) {
                throw new ConflictException("Email already exists", "DUPLICATE_EMAIL");
            }
        }

        if (!isBlank(request.getPhone())) {
            if (userRepository.existsByPhoneAndIdNotAndDeletedAtIsNull(request.getPhone(), id)) {
                throw new ConflictException("Phone already exists", "DUPLICATE_PHONE");
            }
        }

        if (!isBlank(request.getUsername())) {
            if (userRepository.existsByUsernameAndIdNotAndDeletedAtIsNull(request.getUsername(), id)) {
                throw new ConflictException("Username already exists", "DUPLICATE_USERNAME");
            }
        }

        // Protect last active super admin from status change to inactive
        if (request.getStatus() != UserStatus.ACTIVE && isLastSuperAdmin(id)) {
            throw new ConflictException("Cannot deactivate or lock the last active Super Admin", "LAST_SUPER_ADMIN_PROTECTION");
        }

        Map<String, Object> before = snapshot(user);

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setUsername(request.getUsername());
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }
        if (request.getPhoneVerified() != null) {
            user.setPhoneVerified(request.getPhoneVerified());
        }
        if (request.getMfaEnabled() != null) {
            user.setMfaEnabled(request.getMfaEnabled());
        }

        User updated = userRepository.save(user);
        recordAudit("USER_UPDATED", updated, before);
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndDeletedAtIsNull(id);
        return userMapper.toResponse(updated, userRoles);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (isLastSuperAdmin(id)) {
            throw new ConflictException("Cannot delete the last active Super Admin", "LAST_SUPER_ADMIN_PROTECTION");
        }

        Map<String, Object> before = snapshot(user);
        
        // Deactivate instead of physical delete
        user.setStatus(UserStatus.INACTIVE);
        user.delete();
        userRepository.save(user);
        recordAudit("USER_DEACTIVATED", user, before);
    }

    @Override
    @Transactional
    public void lockUser(UUID id) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (isLastSuperAdmin(id)) {
            throw new ConflictException("Cannot lock the last active Super Admin", "LAST_SUPER_ADMIN_PROTECTION");
        }

        Map<String, Object> before = snapshot(user);
        user.setStatus(UserStatus.LOCKED);
        userRepository.save(user);
        recordAudit("USER_LOCKED", user, before);
    }

    @Override
    @Transactional
    public void unlockUser(UUID id) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Map<String, Object> before = snapshot(user);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        recordAudit("USER_UNLOCKED", user, before);
    }

    @Override
    @Transactional
    public String resetPassword(UUID id, ResetPasswordRequest request) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String rawPassword = request.getNewPassword();
        if (isBlank(rawPassword)) {
            rawPassword = request.getTemporaryPassword();
        }
        boolean isGenerated = false;
        if (isBlank(rawPassword)) {
            rawPassword = "Tmp@" + UUID.randomUUID().toString().substring(0, 8);
            isGenerated = true;
        }

        Map<String, Object> before = snapshot(user);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        
        auditService.record("USER_PASSWORD_RESET", "user", "User", id, before, snapshot(user));
        return isGenerated ? rawPassword : "Password reset successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserLookupResponse> lookupUsers(String search, UserStatus status) {
        // Exclude deleted, exclude inactive by default unless requested status is inactive
        UserStatus statusFilter = status != null ? status : UserStatus.ACTIVE;
        List<User> list = userRepository.lookupUsers(search, statusFilter);
        return list.stream().map(userMapper::toLookupResponse).toList();
    }

    // Role assignment logic
    @Override
    @Transactional(readOnly = true)
    public UserRoleAssignmentResponse getUserRoles(UUID userId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndDeletedAtIsNull(userId);
        return userMapper.toAssignmentResponse(user, userRoles);
    }

    @Override
    @Transactional
    public void assignRoles(UUID userId, AssignRolesRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Campus campus = null;
        if (request.getCampusId() != null) {
            campus = campusRepository.findById(request.getCampusId())
                    .filter(c -> c.getDeletedAt() == null)
                    .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));
        }

        for (UUID roleId : request.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                    .filter(r -> r.getDeletedAt() == null && Boolean.TRUE.equals(r.getIsActive()))
                    .orElseThrow(() -> new ResourceNotFoundException("Active role not found for ID: " + roleId));

            // Check if duplicate assignment exists
            List<UserRole> existing = userRoleRepository.findByUserIdAndRoleIdAndDeletedAtIsNull(userId, roleId);
            final UUID fCampusId = campus == null ? null : campus.getId();
            boolean isDuplicate = existing.stream()
                    .anyMatch(ur -> ur.getIsActive() && Objects.equals(ur.getCampus() != null ? ur.getCampus().getId() : null, fCampusId));
            if (isDuplicate) {
                throw new ConflictException("Role is already actively assigned for the specified scope", "DUPLICATE_ROLE_ASSIGNMENT");
            }

            UserRole ur = UserRole.builder()
                    .user(user)
                    .role(role)
                    .campus(campus)
                    .assignedAt(Instant.now())
                    .expiredAt(request.getExpiredAt())
                    .isActive(true)
                    .build();

            userRoleRepository.save(ur);
            auditService.record("USER_ROLE_ASSIGNED", "user", "UserRole", ur.getId(), null,
                    Map.of("userId", userId, "roleId", roleId, "campusId", fCampusId != null ? fCampusId : ""));
        }
    }

    @Override
    @Transactional
    public void removeRole(UUID userId, UUID roleId, UUID campusId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = roleRepository.findById(roleId)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        // Protect last active super admin
        if ("SUPER_ADMIN".equalsIgnoreCase(role.getCode()) && isLastSuperAdmin(userId)) {
            throw new ConflictException("Cannot remove Super Admin role from the last active Super Admin user", "LAST_SUPER_ADMIN_PROTECTION");
        }

        List<UserRole> assignments = userRoleRepository.findByUserIdAndRoleIdAndDeletedAtIsNull(userId, roleId);
        UserRole assignmentToRemove = assignments.stream()
                .filter(ur -> Objects.equals(ur.getCampus() != null ? ur.getCampus().getId() : null, campusId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Role assignment not found for the specified scope"));

        Map<String, Object> before = Map.of("userId", userId, "roleId", roleId, "campusId", campusId != null ? campusId : "");
        assignmentToRemove.setIsActive(false);
        assignmentToRemove.delete();
        userRoleRepository.save(assignmentToRemove);
        auditService.record("USER_ROLE_REMOVED", "user", "UserRole", assignmentToRemove.getId(), before, null);
    }

    @Override
    @Transactional
    public void updateRoleAssignment(UUID userId, UUID roleId, UpdateRoleAssignmentRequest request) {
        userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        roleRepository.findById(roleId)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        // Since the UI maps assignments individually, let's find the assignment by the campus scope in the request
        List<UserRole> assignments = userRoleRepository.findByUserIdAndRoleIdAndDeletedAtIsNull(userId, roleId);
        UserRole assignment = assignments.stream()
                .filter(ur -> Objects.equals(ur.getCampus() != null ? ur.getCampus().getId() : null, request.getCampusId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User role assignment not found for this role and campus scope"));

        Map<String, Object> before = Map.of("isActive", assignment.getIsActive(), "expiredAt", assignment.getExpiredAt() != null ? assignment.getExpiredAt() : "");

        if (request.getIsActive() != null) {
            assignment.setIsActive(request.getIsActive());
        }
        if (request.getExpiredAt() != null) {
            assignment.setExpiredAt(request.getExpiredAt());
        }

        userRoleRepository.save(assignment);
        auditService.record("USER_ROLE_ASSIGNMENT_UPDATED", "user", "UserRole", assignment.getId(), before,
                Map.of("isActive", assignment.getIsActive(), "expiredAt", assignment.getExpiredAt() != null ? assignment.getExpiredAt() : ""));
    }

    private boolean isLastSuperAdmin(UUID userId) {
        List<UserRole> assignments = userRoleRepository.findByUserIdAndDeletedAtIsNull(userId);
        boolean hasSuperAdmin = assignments.stream()
                .anyMatch(ur -> ur.getIsActive() && "SUPER_ADMIN".equalsIgnoreCase(ur.getRole().getCode()));
        if (!hasSuperAdmin) {
            return false;
        }

        // Count active Super Admins
        long activeSuperAdmins = userRoleRepository.findAll().stream()
                .filter(ur -> ur.getDeletedAt() == null && ur.getIsActive() 
                        && "SUPER_ADMIN".equalsIgnoreCase(ur.getRole().getCode()) 
                        && ur.getUser().getStatus() == UserStatus.ACTIVE 
                        && ur.getUser().getDeletedAt() == null)
                .map(ur -> ur.getUser().getId())
                .distinct()
                .count();

        return activeSuperAdmins <= 1;
    }

    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    private Map<String, Object> snapshot(User u) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("fullName", u.getFullName());
        map.put("email", u.getEmail());
        map.put("phone", u.getPhone());
        map.put("username", u.getUsername());
        map.put("status", u.getStatus());
        map.put("mfaEnabled", u.getMfaEnabled());
        return map;
    }

    private void recordAudit(String action, User u, Map<String, Object> before) {
        auditService.record(action, "user", "User", u.getId(), before, snapshot(u));
    }
}
