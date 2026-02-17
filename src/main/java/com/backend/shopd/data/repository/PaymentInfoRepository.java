package com.backend.shopd.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.shopd.data.entity.PaymentInfoEntity;

@Repository
public interface PaymentInfoRepository extends JpaRepository<PaymentInfoEntity, String> {
    List<PaymentInfoEntity> findByUserId(String userId);
    Optional<PaymentInfoEntity> findByUserIdAndIsDefault(String userId, Boolean isDefault);
}
