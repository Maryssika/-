package com.ovz.platform.repositories.chat;

import com.ovz.platform.models.chat.ChatMessage;
import com.ovz.platform.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByReceiverAndReadFalse(User receiver);

    // Явный JPQL-запрос для получения всех сообщений между двумя пользователями
    @Query("SELECT m FROM ChatMessage m WHERE (m.sender = :userA AND m.receiver = :userB) OR (m.sender = :userB AND m.receiver = :userA) ORDER BY m.sentAt ASC")
    List<ChatMessage> findConversationBetween(@Param("userA") User userA, @Param("userB") User userB);

    @Query("SELECT DISTINCT m.sender FROM ChatMessage m WHERE m.receiver = :user " +
            "UNION " +
            "SELECT DISTINCT m.receiver FROM ChatMessage m WHERE m.sender = :user")
    List<User> findConversationPartners(@Param("user") User user);
}