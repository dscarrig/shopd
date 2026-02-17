package com.backend.shopd.web.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.shopd.data.entity.AddressEntity;
import com.backend.shopd.data.entity.UserEntity;
import com.backend.shopd.service.UserService;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public List<UserEntity> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserEntity getUserById(@PathVariable UUID id){
        return userService.getUserById(id);
    }

    @GetMapping("/user-id/{username}")
    public UUID getUserIdByUsername(@PathVariable String username){
        UUID userId = userService.getUserIdByUsername(username);
        return userId;
    }

    @PostMapping
    public UserEntity createUser(@RequestBody UserEntity user){
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public UserEntity updateUser(@PathVariable UUID id, @RequestBody UserEntity user){
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id){
        userService.deleteUser(id);
    }

    @PostMapping("/create-new-address/{id}")
    public void createNewAddress(@PathVariable UUID id, @RequestBody String addressDetails){
        System.out.println("Creating new address for user ID: " + id + " with details: " + addressDetails);
        userService.createNewAddress(id, addressDetails);
    }

    @PutMapping("/update-address/{id}")
    public void updateAddress(@PathVariable UUID id, @RequestBody UUID addressId){
        userService.updateDefaultAddress(id, addressId);
    }

    @GetMapping("/account-type/{id}")
    public String getCurrentAccountDetails(@PathVariable UUID id) {
        UserEntity user = userService.getUserById(id);
        return user.getAccountType();
    }

    @GetMapping("/all-addresses/{id}")
    public List<AddressEntity> getAllAddresses(@PathVariable UUID id) {
        return userService.getAllAddresses(id);
    }
    
    @GetMapping("/get-default-address/{id}")
    public AddressEntity getDefaultAddress(@PathVariable UUID id) {
        return userService.getDefaultAddress(id);
    }
    
    @DeleteMapping("/delete-address/{id}")
    public void deleteAddress(@PathVariable UUID id) {
        userService.deleteAddress(id);
    }

    @DeleteMapping("/delete-account/{id}")
    public void deleteAccount(@PathVariable UUID id) {
        userService.deleteUser(id);
    }

    @PostMapping("/set-new-default-address")
    public void setDefaultAddress(@RequestBody String entity) {
        System.out.println("Setting new default address with data: " + entity);
        userService.updateDefaultAddress(UUID.fromString(entity.split(",")[0]), UUID.fromString(entity.split(",")[1]));
    }
}