package com.backend.shopd.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.shopd.data.entity.ShopdItem;
import com.backend.shopd.data.repository.ShopdItemRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("api/cart")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8081"})
public class CartController {
    
    @Autowired
    private CartItemUtils cartItemUtils;

    @Autowired
    private ShopdItemRepository shopdItemRepository;

    @PostMapping("/add/{user_id}")
    public ResponseEntity<ShopdItem> addItemToCart(@PathVariable String user_id, @RequestBody String itemId)
	{
        System.out.println("Adding item " + itemId + " to user " + user_id + " cart.");
        
        try {
            java.util.UUID uuid = java.util.UUID.fromString(itemId);
            System.out.println("Parsed UUID: " + uuid);
            
            shopdItemRepository.findById(uuid)
                    .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format: " + itemId);
            return new ResponseEntity<ShopdItem>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.out.println("Item not found in database: " + itemId);
            return new ResponseEntity<ShopdItem>(HttpStatus.NOT_FOUND);
        }
        
        cartItemUtils.addItem(user_id, itemId);
        System.out.println("Added item " + itemId + " to user " + user_id + " cart.");
		return new ResponseEntity<ShopdItem>(HttpStatus.OK);
    }

    @PostMapping("/copy/{user_id}")
    public ResponseEntity<ShopdItem> copyItemsToCart(@PathVariable String user_id, @RequestBody String copyFromUser)
	{
        try {
            cartItemUtils.getUsersItems(copyFromUser);
        } catch (Exception e) {
            return new ResponseEntity<ShopdItem>(HttpStatus.NOT_FOUND);
        }
		cartItemUtils.copyCart(copyFromUser, user_id);
		return new ResponseEntity<ShopdItem>(HttpStatus.OK);
	}

    @GetMapping("/items/{user_id}")
    public List<ShopdItem> getAllCartItems(@PathVariable String user_id)
	{
		List<ShopdItem> result = new ArrayList<ShopdItem>();
		List<String> itemIds = cartItemUtils.getUsersItems(user_id);
		if (itemIds != null)
		{
			for (int i = 0; i < itemIds.size(); i++)
			{
				result.add(shopdItemRepository.findById(java.util.UUID.fromString(itemIds.get(i).toString())).get());
			}
		}
		return result;
	}
    
    @DeleteMapping("/remove/{user_id}/{item_id}")
    public ResponseEntity<ShopdItem> removeCartItem(@PathVariable String user_id, @PathVariable String item_id)
	{
        try {
            shopdItemRepository.findById(java.util.UUID.fromString(item_id))
                    .orElseThrow(() -> new RuntimeException("Item not found with id: " + item_id));
        } catch (Exception e) {
            return new ResponseEntity<ShopdItem>(HttpStatus.NOT_FOUND);
        }
		cartItemUtils.removeItem(user_id, item_id);
		return new ResponseEntity<ShopdItem>(HttpStatus.OK);
	}

    @DeleteMapping("/clear/{user_id}")
    public ResponseEntity<ShopdItem> clearCartItems(@PathVariable String user_id)
    {
        cartItemUtils.clearItems(user_id);
        return new ResponseEntity<ShopdItem>(HttpStatus.OK);
    }

    @GetMapping("/total/{user_id}")
    public ResponseEntity<Double> getTotalPrice(@PathVariable String user_id)
    {
        List<ShopdItem> allItems = getAllCartItems(user_id);
        double total = cartItemUtils.getTotalPrice(allItems);
        return new ResponseEntity<Double>(total, HttpStatus.OK);
    }

    @GetMapping("item-count/{user_id}")
    public int totalItems(@PathVariable String user_id)
	{
		if (cartItemUtils.getUsersItems(user_id) != null)
			return cartItemUtils.getUsersItems(user_id).size();
		else
			return 0;
	}
}
