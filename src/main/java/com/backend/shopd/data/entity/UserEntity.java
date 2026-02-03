package com.backend.shopd.data.entity;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "email")
    private String email;
    @Column(name = "account_type", nullable = false)
    private String accountType;
    @Column(name = "payment_info", nullable = true, columnDefinition = "TEXT")
    @JdbcType(VarcharJdbcType.class)
    private List<String> paymentInfo;
    @Column(name = "default_payment", nullable = true)
    private String defaultPayment;
    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;

    public String getUsername() {
        return username;
    }
}

