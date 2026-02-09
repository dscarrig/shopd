package com.backend.shopd.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.shopd.data.entity.PaymentInfoEntity;

@Repository
public interface PaymentInfoRepository extends JpaRepository<PaymentInfoEntity, String> {
    
}
