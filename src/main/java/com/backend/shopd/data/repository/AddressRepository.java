package com.backend.shopd.data.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.backend.shopd.data.entity.AddressEntity;
import com.backend.shopd.data.entity.UserEntity;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, UUID>  {
    List<AddressEntity> findByUser(UserEntity user);
    List<AddressEntity> findByUserId(UUID userId);
    Optional<AddressEntity> findByUserAndIsDefault(UserEntity user, Boolean isDefault);
    Optional<AddressEntity> findByUserIdAndIsDefault(UUID userId, Boolean isDefault);
}
