// EmailVerificationTokenRepository.java
package com.fleetmanagement.userservice.repository;

import com.fleetmanagement.userservice.domain.entity.EmailVerificationToken;
import com.fleetmanagement.userservice.domain.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenAndTokenType(String token, TokenType tokenType);

    @Query("SELECT t FROM EmailVerificationToken t WHERE t.userId = :userId AND t.tokenType = :tokenType AND t.isUsed = false AND t.expiryDate > :now")
    Optional<EmailVerificationToken> findActiveTokenByUserIdAndType(@Param("userId") UUID userId,
                                                                    @Param("tokenType") TokenType tokenType,
                                                                    @Param("now") LocalDateTime now);

    default Optional<EmailVerificationToken> findActiveTokenByUserIdAndType(UUID userId, TokenType tokenType) {
        return findActiveTokenByUserIdAndType(userId, tokenType, LocalDateTime.now());
    }

    @Query("SELECT t FROM EmailVerificationToken t WHERE t.userId = :userId AND t.tokenType = :tokenType AND t.isUsed = false AND t.expiryDate > :now")
    List<EmailVerificationToken> findActiveTokensByUserIdAndType(@Param("userId") UUID userId,
                                                                 @Param("tokenType") TokenType tokenType,
                                                                 @Param("now") LocalDateTime now);

    default List<EmailVerificationToken> findActiveTokensByUserIdAndType(UUID userId, TokenType tokenType) {
        return findActiveTokensByUserIdAndType(userId, tokenType, LocalDateTime.now());
    }

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiryDate < :cutoffDate")
    int deleteExpiredTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(t) FROM EmailVerificationToken t WHERE t.userId = :userId AND t.tokenType = :tokenType AND t.createdAt > :since")
    int countTokensCreatedSince(@Param("userId") UUID userId,
                                @Param("tokenType") TokenType tokenType,
                                @Param("since") LocalDateTime since);
}
