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
        System.out.println("Retrieved user ID: " + userId);
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

    @PutMapping("/update-address/{id}")
    public void updateAddress(@PathVariable UUID id, @RequestBody String addressInfo){
        userService.updateAddress(id, addressInfo);
    }

    @GetMapping("/account-details/{id}")
    public String getCurrentAccountDetails(@PathVariable UUID id) {
        UserEntity user = userService.getUserById(id);
        return user.getAccountType();
    }

    @GetMapping("/all-account-details/{id}")
    public List<String> getAllAccountDetails(@PathVariable UUID id) {
        UserEntity user = userService.getUserById(id);
        return user.getAddressInfo();
    }
    
    @GetMapping("/get-address/{id}")
    public String getAddress(@PathVariable UUID id) {
        UserEntity user = userService.getUserById(id);
        return user.getDefaultAddress();
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
    public String setDefaultAddress(@RequestBody String entity) {
        //TODO: process POST request
        return entity;
    }
}

