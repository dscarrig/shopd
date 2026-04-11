package com.backend.shopd.data.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.shopd.data.entity.OrderEntity;
import com.backend.shopd.data.entity.OrderItemEntity;
import com.backend.shopd.data.entity.ShopdItem;
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT o FROM OrderEntity o JOIN o.items i WHERE i.itemId = :itemId")
    List<OrderEntity> findByItemId(@Param("itemId") UUID itemId);

    @Query("SELECT o FROM OrderEntity o JOIN o.items i WHERE o.userId = :userId AND i.itemId = :itemId ORDER BY o.createdAt DESC")
    List<OrderEntity> findByUserIdAndItemId(@Param("userId") UUID userId, @Param("itemId") UUID itemId);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM OrderEntity o JOIN o.items i WHERE o.userId = :userId AND i.itemId = :itemId")
    boolean existsByUserIdAndItemId(@Param("userId") UUID userId, @Param("itemId") UUID itemId);

    @Query("SELECT COUNT(i) FROM OrderEntity o JOIN o.items i WHERE o.userId = :userId AND i.itemId = :itemId")
    long countItemsByUserIdAndItemId(@Param("userId") UUID userId, @Param("itemId") UUID itemId);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM OrderEntity o JOIN o.items i WHERE o.userId = :userId AND i.itemId = :itemId")
    long sumItemQuantityByUserIdAndItemId(@Param("userId") UUID userId, @Param("itemId") UUID itemId);

    @Query("SELECT i FROM OrderEntity o JOIN o.items i WHERE i.itemId IN (SELECT s.id FROM ShopdItem s WHERE s.userId = :userId)")
    List<OrderItemEntity> findOrderItemsByShopdItemUserId(@Param("userId") UUID userId);

    @Query("SELECT o FROM OrderEntity o JOIN o.items i WHERE i.id = :orderItemId")
    Optional<OrderEntity> findByOrderItemId(@Param("orderItemId") UUID orderItemId);
}
