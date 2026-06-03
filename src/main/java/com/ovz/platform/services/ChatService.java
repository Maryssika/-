package com.ovz.platform.services;

import com.ovz.platform.models.chat.ChatMessage;
import com.ovz.platform.models.user.User;
import com.ovz.platform.repositories.chat.ChatMessageRepository;
import com.ovz.platform.repositories.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ChatService {

    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatService(ChatMessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public ChatMessage sendMessage(Long senderId, Long receiverId, String content) {
        User sender = userRepository.findById(senderId).orElseThrow(
                () -> new IllegalArgumentException("Отправитель не найден"));
        User receiver = userRepository.findById(receiverId).orElseThrow(
                () -> new IllegalArgumentException("Получатель не найден"));

        ChatMessage msg = new ChatMessage();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(content);
        msg.setSentAt(LocalDateTime.now());
        msg.setRead(false);
        return messageRepository.save(msg);
    }

    public List<ChatMessage> getConversation(Long userId1, Long userId2) {
        User u1 = userRepository.findById(userId1).orElseThrow();
        User u2 = userRepository.findById(userId2).orElseThrow();
        return messageRepository.findConversationBetween(u1, u2);
    }

    public List<User> getChatPartners(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return messageRepository.findConversationPartners(user);
    }

    public void markMessagesAsRead(Long userId, Long partnerId) {
        User receiver = userRepository.findById(userId).orElseThrow();
        User sender = userRepository.findById(partnerId).orElseThrow();

        List<ChatMessage> unread = messageRepository.findByReceiverAndReadFalse(receiver);
        unread.stream()
                .filter(m -> m.getSender().getId().equals(sender.getId()))
                .forEach(m -> m.setRead(true));
        messageRepository.saveAll(unread);
    }

    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return messageRepository.findByReceiverAndReadFalse(user).size();
    }
}