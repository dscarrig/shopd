package com.backend.shopd.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.backend.shopd.data.entity.ShopdItem;
import com.backend.shopd.data.repository.ShopdItemRepository;

@Service
public class ShopdItemService {
    private final ShopdItemRepository shopdItemRepository;

    public ShopdItemService(ShopdItemRepository shopdItemRepository) {
        this.shopdItemRepository = shopdItemRepository;
    }

    public List<ShopdItem> getAllItems() {
        return shopdItemRepository.findAll();
    }

    public ShopdItem getItemById(UUID id) {
        ShopdItem item = shopdItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
        return item;
    }

    public ShopdItem createItem(ShopdItem item) {
        return shopdItemRepository.save(item);
    }

    public void deleteItem(UUID id) {
        shopdItemRepository.deleteById(id);
    }

    public ShopdItem updateItem(UUID id, ShopdItem updatedItem) {
        ShopdItem existingItem = shopdItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));

        existingItem.setName(updatedItem.getName());
        existingItem.setDescription(updatedItem.getDescription());
        existingItem.setPrice(updatedItem.getPrice());
        existingItem.setImageUrl(updatedItem.getImageUrl());
        existingItem.setCategory(updatedItem.getCategory());
        existingItem.setAvailable(updatedItem.isAvailable());
        existingItem.setQuantity(updatedItem.getQuantity());
        existingItem.setUserId(updatedItem.getUserId());

        return shopdItemRepository.save(existingItem);
    }
}
