package com.backend.shopd.web.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.shopd.data.entity.MessageEntity;
import com.backend.shopd.service.MessageService;

@RestController
@RequestMapping("api/messages")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8081"})
public class MessageApiController {

    private final MessageService messageService;

    public MessageApiController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/by-id/{message_id}")
    public ResponseEntity<MessageEntity> getMessageById(@PathVariable UUID message_id) {
        MessageEntity message = messageService.getMessageById(message_id);
        if (message != null) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/send/{sender_user_id}/{receiver_user_id}")
    public ResponseEntity<MessageEntity> sendMessage(
            @PathVariable UUID sender_user_id,
            @PathVariable UUID receiver_user_id,
            @RequestBody MessageEntity message) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(messageService.sendMessage(sender_user_id, receiver_user_id, message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/receive/{receiver_user_id}")
    public ResponseEntity<List<MessageEntity>> receiveMessages(@PathVariable UUID receiver_user_id) {
        return ResponseEntity.ok(messageService.getMessagesForUser(receiver_user_id));
    }

    @DeleteMapping("/delete/{message_id}")
    public ResponseEntity<String> deleteMessage(@PathVariable UUID message_id) {
        try {
            messageService.deleteMessage(message_id);
            return ResponseEntity.ok("Message deleted successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/mark-as-read/{message_id}")
    public ResponseEntity<MessageEntity> markAsRead(@PathVariable UUID message_id) {
        try {
            return ResponseEntity.ok(messageService.markAsRead(message_id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/edit/{message_id}")
    public ResponseEntity<MessageEntity> editMessage(
            @PathVariable UUID message_id,
            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(messageService.editMessage(message_id, body.get("subject"), body.get("content")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
