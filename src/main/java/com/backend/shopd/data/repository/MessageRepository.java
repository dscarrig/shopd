package com.backend.shopd.data.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.shopd.data.entity.MessageEntity;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

    List<MessageEntity> findByReceiverUserId(UUID receiverUserId);

    List<MessageEntity> findByUserId(UUID userId);

    List<MessageEntity> findByRead(boolean read);
}
