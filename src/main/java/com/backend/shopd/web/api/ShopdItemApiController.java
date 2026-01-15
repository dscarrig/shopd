package com.backend.shopd.web.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.shopd.data.entity.ShopdItem;
import com.backend.shopd.service.ShopdItemService;

@RestController
@RequestMapping("/api/items")
public class ShopdItemApiController {
    private final ShopdItemService shopdItemService;

    public ShopdItemApiController(ShopdItemService shopdItemService) {
        this.shopdItemService = shopdItemService;
    }

    @GetMapping
    public List<ShopdItem> getAllItems(){
        return shopdItemService.getAllItems();
    }

    @GetMapping("/{id}")
    public ShopdItem getItemById(@PathVariable UUID id){
        return shopdItemService.getItemById(id);
    }

    @PostMapping
    public ShopdItem createItem(@RequestBody ShopdItem item){
        return shopdItemService.createItem(item);
    }

    @PutMapping("/{id}")
    public ShopdItem updateItem(@PathVariable UUID id, @RequestBody ShopdItem item){
        return shopdItemService.updateItem(id, item);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable UUID id){
        shopdItemService.deleteItem(id);
    }
}
