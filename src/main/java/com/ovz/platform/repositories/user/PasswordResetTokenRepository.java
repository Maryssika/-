package com.ovz.platform.repositories.user;

import com.ovz.platform.models.user.PasswordResetToken;
import com.ovz.platform.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}