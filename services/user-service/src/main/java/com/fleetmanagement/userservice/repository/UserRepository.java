// UserRepository.java
package com.fleetmanagement.userservice.repository;

import com.fleetmanagement.userservice.domain.entity.User;
import com.fleetmanagement.userservice.domain.enums.UserRole;
import com.fleetmanagement.userservice.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // Basic find methods
    Optional<User> findByUsername(String username);


    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    // Existence checks
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);

    // Company-specific queries
    List<User> findByCompanyId(UUID companyId);

    Page<User> findByCompanyId(UUID companyId, Pageable pageable);

    List<User> findByCompanyIdAndRole(UUID companyId, UserRole role);

    Page<User> findByCompanyIdAndRole(UUID companyId, UserRole role, Pageable pageable);

    List<User> findByCompanyIdAndStatus(UUID companyId, UserStatus status);

    Page<User> findByCompanyIdAndStatus(UUID companyId, UserStatus status, Pageable pageable);

    // Role-based queries
    List<User> findByRole(UserRole role);

    Page<User> findByRole(UserRole role, Pageable pageable);

    List<User> findByRoleIn(List<UserRole> roles);

    // Status-based queries
    List<User> findByStatus(UserStatus status);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    List<User> findByStatusIn(List<UserStatus> statuses);

    // Search queries
    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:companyId IS NULL OR u.companyId = :companyId)")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm,
                           @Param("companyId") UUID companyId,
                           Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.companyId = :companyId AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchUsersByCompany(@Param("companyId") UUID companyId,
                                    @Param("searchTerm") String searchTerm,
                                    Pageable pageable);

    // Security-related queries
    List<User> findByAccountLockedUntilBefore(LocalDateTime dateTime);

    List<User> findByFailedLoginAttemptsGreaterThan(Integer attempts);

    List<User> findByEmailVerificationExpiryBefore(LocalDateTime dateTime);

    List<User> findByPasswordResetExpiryBefore(LocalDateTime dateTime);

    // Update operations
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("lastLogin") LocalDateTime lastLogin);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.id = :userId")
    void updateFailedLoginAttempts(@Param("userId") UUID userId, @Param("attempts") Integer attempts);

    @Modifying
    @Query("UPDATE User u SET u.accountLockedUntil = :lockUntil WHERE u.id = :userId")
    void updateAccountLockStatus(@Param("userId") UUID userId, @Param("lockUntil") LocalDateTime lockUntil);

    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true, u.emailVerificationToken = null, " +
            "u.emailVerificationExpiry = null WHERE u.id = :userId")
    void markEmailAsVerified(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :passwordHash, u.lastPasswordChange = :changeTime, " +
            "u.passwordResetToken = null, u.passwordResetExpiry = null WHERE u.id = :userId")
    void updatePassword(@Param("userId") UUID userId,
                        @Param("passwordHash") String passwordHash,
                        @Param("changeTime") LocalDateTime changeTime);

    // Statistics queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.companyId = :companyId")
    long countByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.companyId = :companyId AND u.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.companyId = :companyId AND u.role = :role")
    long countByCompanyIdAndRole(@Param("companyId") UUID companyId, @Param("role") UserRole role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin >= :since")
    long countActiveUsersSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(u) FROM User u WHERE u.companyId = :companyId AND u.lastLogin >= :since")
    long countActiveUsersByCompanySince(@Param("companyId") UUID companyId, @Param("since") LocalDateTime since);

    // Cleanup operations
    @Modifying
    @Query("DELETE FROM User u WHERE u.status = :status AND u.createdAt < :createdBefore")
    void deleteByStatusAndCreatedBefore(@Param("status") UserStatus status,
                                        @Param("createdBefore") LocalDateTime createdBefore);

    @Modifying
    @Query("UPDATE User u SET u.emailVerificationToken = null, u.emailVerificationExpiry = null " +
            "WHERE u.emailVerificationExpiry < :expiredBefore")
    void clearExpiredEmailVerificationTokens(@Param("expiredBefore") LocalDateTime expiredBefore);

    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = null, u.passwordResetExpiry = null " +
            "WHERE u.passwordResetExpiry < :expiredBefore")
    void clearExpiredPasswordResetTokens(@Param("expiredBefore") LocalDateTime expiredBefore);
}
