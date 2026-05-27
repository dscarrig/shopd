package com.backend.shopd.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.shopd.data.entity.MessageEntity;
import com.backend.shopd.data.repository.MessageRepository;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public List<MessageEntity> getMessagesForUser(UUID receiverUserId) {
        System.out.println("Fetching messages for user: " + receiverUserId);
        return messageRepository.findByReceiverUserId(receiverUserId);
    }

    @Transactional
    public MessageEntity sendMessage(UUID senderUserId, UUID receiverUserId, MessageEntity message) {
        System.out.println("Sending message from user: " + senderUserId + " to user: " + receiverUserId);
        System.out.println("Message content: " + message.getContent());
        message.setId(null);
        message.setUserId(senderUserId);
        message.setReceiverUserId(receiverUserId);
        message.setTimestamp(System.currentTimeMillis());
        message.setRead(false);
        return messageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(UUID messageId) {
        System.out.println("Deleting message with ID: " + messageId);
        if (!messageRepository.existsById(messageId)) {
            throw new IllegalArgumentException("Message not found: " + messageId);
        }
        messageRepository.deleteById(messageId);
    }

    @Transactional
    public MessageEntity markAsRead(UUID messageId) {
        System.out.println("Marking message as read with ID: " + messageId);
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        message.setRead(true);
        return messageRepository.save(message);
    }

    @Transactional
    public MessageEntity editMessage(UUID messageId, String subject, String content) {
        System.out.println("Editing message with ID: " + messageId);
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        if (subject != null && !subject.isBlank()) {
            message.setSubject(subject);
        }
        if (content != null && !content.isBlank()) {
            message.setContent(content);
        }
        return messageRepository.save(message);
    }
}
