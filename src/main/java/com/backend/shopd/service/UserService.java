package com.backend.shopd.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.backend.shopd.data.entity.AddressEntity;
import com.backend.shopd.data.entity.UserEntity;
import com.backend.shopd.data.repository.AddressRepository;
import com.backend.shopd.data.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserService(UserRepository userRepository, AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity getUserById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return user;
    }

    public UserEntity getUserByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return user;
    }

    public UUID getUserIdByUsernameOrEmail(String usernameOrEmail) {
        UserEntity user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new RuntimeException("User not found with username or email: " + usernameOrEmail));
        return user.getId();
    }

    public UserEntity createUser(UserEntity user) {
        return userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    public UserEntity updateUser(UUID id, UserEntity updatedUser) {
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setAccountType(updatedUser.getAccountType());

        return userRepository.save(existingUser);
    }

    public void updateDefaultAddress(UUID userId, UUID addressId) {
        UserEntity existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Get all addresses for the user
        List<AddressEntity> userAddresses = addressRepository.findByUser(existingUser);
        
        // Set all addresses to not default
        for (AddressEntity address : userAddresses) {
            address.setIsDefault(false);
        }
        addressRepository.saveAll(userAddresses);
        
        // Set the specified address as default
        AddressEntity defaultAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));
        defaultAddress.setIsDefault(true);
        addressRepository.save(defaultAddress);
    }

    public void deleteAddress(UUID addressId) {
        AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));

        addressRepository.delete(address);
    }

    public AddressEntity getDefaultAddress(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return addressRepository.findByUserAndIsDefault(user, true)
                .orElseGet(() -> {
                    AddressEntity newAddress = new AddressEntity();
                    newAddress.setUser(user);
                    newAddress.setFullName("");
                    newAddress.setStreet("");
                    newAddress.setCity("");
                    newAddress.setState("");
                    newAddress.setZipCode("");
                    newAddress.setCountry("");
                    newAddress.setIsDefault(true);
                    return newAddress;
                });
    }

    public List<AddressEntity> getAllAddresses(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return addressRepository.findByUser(user);
    }

    public AddressEntity createNewAddress(UUID userId, String addressDetails) {
        // Get the user entity
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        try {
            // Parse JSON to AddressEntity
            AddressEntity newAddress = objectMapper.readValue(addressDetails, AddressEntity.class);
            
            // Set the user relationship
            newAddress.setUser(user);
            
            // If this is the first address for the user, make it default
            List<AddressEntity> existingAddresses = addressRepository.findByUser(user);
            if (existingAddresses.isEmpty()) {
                System.out.println("No existing addresses found for user ID: " + userId + ". Setting new address as default.");
                newAddress.setIsDefault(true);
            } else if (newAddress.getIsDefault() == null) {
                newAddress.setIsDefault(false);
            }
            
            // If the new address is marked as default, unset any existing default
            if (Boolean.TRUE.equals(newAddress.getIsDefault())) {
                System.out.println("New address is marked as default for user ID: " + userId + ". Unsetting existing default addresses.");
                for (AddressEntity address : existingAddresses) {
                    address.setIsDefault(false);
                }
                addressRepository.saveAll(existingAddresses);
            }
            
            // Save and return the new address
            return addressRepository.save(newAddress);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse address details: " + e.getMessage(), e);
        }
    }

    public String getUsernameById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return user.getUsername();
    }

    public List<UserEntity> getAllUsersWithListings() {
        return userRepository.findAllUsersWithListings();
    }

}
