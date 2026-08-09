package com.clearledger.user_service.repository;

import com.clearledger.user_service.entity.RefreshToken;
import com.clearledger.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    // Revoke all existing tokens for a user before issuing a new one
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user AND r.revoked = false")
    void revokeAllUserTokens(User user);
}

//revokeAllUserTokens uses JPQL (@Query) because Spring can't infer this update logic from the method name alone.
// @Modifying tells JPA this is a write operation, not a select.
