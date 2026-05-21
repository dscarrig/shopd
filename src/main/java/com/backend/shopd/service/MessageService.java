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
        return messageRepository.findByReceiverUserId(receiverUserId);
    }

    @Transactional
    public MessageEntity sendMessage(UUID senderUserId, UUID receiverUserId, MessageEntity message) {
        message.setUserId(senderUserId);
        message.setReceiverUserId(receiverUserId);
        message.setTimestamp(System.currentTimeMillis());
        message.setRead(false);
        return messageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(UUID messageId) {
        if (!messageRepository.existsById(messageId)) {
            throw new IllegalArgumentException("Message not found: " + messageId);
        }
        messageRepository.deleteById(messageId);
    }

    @Transactional
    public MessageEntity markAsRead(UUID messageId) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        message.setRead(true);
        return messageRepository.save(message);
    }

    @Transactional
    public MessageEntity editMessage(UUID messageId, String subject, String content) {
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
