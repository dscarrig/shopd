package com.backend.shopd.data.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "addresses")
@Data
public class AddressEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "address_id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "full_name", nullable = true)
    private String fullName;
    @Column(name = "street", nullable = true)
    private String street;
    @Column(name = "street_line2", nullable = true)
    private String streetLine2;
    @Column(name = "city", nullable = true)
    private String city;
    @Column(name = "state", nullable = true)
    private String state;
    @Column(name = "zip_code", nullable = true)
    private String zipCode;
    @Column(name = "country", nullable = true)
    private String country;
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
