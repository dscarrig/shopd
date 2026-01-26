package com.backend.shopd.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.shopd.data.entity.ShopdItem;

@Service
public class CartItemUtils {
    private HashMap<String, ArrayList<String>> customerCarts = new HashMap<>();
	
	public List<String> getUsersItems(String customer)
	{
		return customerCarts.get(customer);
	}
	
	public void addItem(String customer, String itemId)
	{
		ArrayList<String> currentItems;
		
		if(customerCarts.containsKey(customer))
			currentItems = customerCarts.get(customer);
		else
			currentItems = new ArrayList<String>();
		
		currentItems.add(itemId);
		customerCarts.put(customer, currentItems);
		
		System.out.println("Added " + itemId + " item to " + customer);
	}
	
	public void removeItem(String customer, String itemId)
	{
		ArrayList<String> currentItems = customerCarts.get(customer);
		currentItems.remove(itemId);
		customerCarts.put(customer, currentItems);
	}
	
	public void clearItems(String customer)
	{
		customerCarts.put(customer, new ArrayList<String>());
	}
	
	public void copyCart(String customer, String otherCustomer)
	{
		ArrayList<String> itemsToCopy;
		
		if(customerCarts.containsKey(customer))
		{
			System.out.println("Copying from " + customer + " to " + otherCustomer);
			itemsToCopy = customerCarts.get(customer);
		}
		else
			itemsToCopy = new ArrayList<String>();
		
		for(int i = 0; i < itemsToCopy.size(); i++)
			addItem(otherCustomer, itemsToCopy.get(i));
	}

    public double getTotalPrice(List<ShopdItem> allItems) {
		double result = 0;

		for (int i = 0; i < allItems.size(); i++)
		{
			result = result + allItems.get(i).getPrice();
		}

		return result;
    }
}
