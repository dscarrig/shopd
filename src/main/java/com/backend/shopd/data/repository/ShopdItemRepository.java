package com.backend.shopd.data.repository;

import java.util.UUID;
import com.backend.shopd.data.entity.ShopdItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopdItemRepository extends JpaRepository<ShopdItem, UUID> {
    
}
