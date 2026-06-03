package com.ovz.platform.controllers;

import com.ovz.platform.models.chat.ChatMessage;
import com.ovz.platform.models.user.User;
import com.ovz.platform.models.user.UserRole;
import com.ovz.platform.services.ChatService;
import com.ovz.platform.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;

    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }


    // Список диалогов текущего пользователя
    @GetMapping
    public String conversations(Model model, Authentication auth) {
        User current = userService.findByEmail(auth.getName());
        List<User> partners = chatService.getChatPartners(current.getId());
        model.addAttribute("partners", partners);
        return "/conversations";
    }

    // Чат с конкретным собеседником
    @GetMapping("/with/{userId}")
    public String chat(@PathVariable Long userId, Model model, Authentication auth) {
        User current = userService.findByEmail(auth.getName());
        User partner = userService.findById(userId);
        List<ChatMessage> messages = chatService.getConversation(current.getId(), partner.getId());
        chatService.markMessagesAsRead(current.getId(), partner.getId());

        model.addAttribute("partner", partner);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUserId", current.getId());
        model.addAttribute("currentUserEmail", current.getEmail());

        // Определяем URL для кнопки "Назад" без отдельного метода
        String backUrl;
        if (current.getRole() == UserRole.TEACHER) {
            backUrl = "/teacher/dashboard";
        } else if (current.getRole() == UserRole.PARENT) {
            backUrl = "/parent/dashboard";
        } else {
            backUrl = "/profile";
        }
        model.addAttribute("backUrl", backUrl);

        System.out.println("Current user ID: " + current.getId() + ", email: " + current.getEmail());
        for (ChatMessage msg : messages) {
            System.out.println("Message from: " + msg.getSender().getId() + " -> " + msg.getContent());
        }

        return "/chat";
    }

    // Отправка сообщения (AJAX или обычный POST)
    @PostMapping("/send")
    public String sendMessage(@RequestParam Long receiverId, @RequestParam String content, Authentication auth) {
        User sender = userService.findByEmail(auth.getName());
        chatService.sendMessage(sender.getId(), receiverId, content);
        return "redirect:/chat/with/" + receiverId;
    }
}
