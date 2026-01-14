package com.backend.shopd.jwt;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.backend.shopd.data.entity.UserEntity;
import com.backend.shopd.data.repository.UserRepository;

@Service
public class JwtInMemoryUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    static List<UserEntity> inMemoryUserList = new ArrayList<>();

    private long userCount;

    public void initialize() {
        userCount = userRepository.count();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadUserByUsername'");
    }
    
}
