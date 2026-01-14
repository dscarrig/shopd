package com.backend.shopd.data.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.backend.shopd.data.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

}
