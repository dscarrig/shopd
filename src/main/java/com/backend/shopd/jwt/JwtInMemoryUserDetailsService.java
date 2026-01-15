package com.backend.shopd.jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    static List<JwtUserDetails> inMemoryUserList = new ArrayList<>();

    private long userCount;

    public JwtInMemoryUserDetailsService()
	{
		userCount = 20001;
	}

    public void initialize() {
        List<UserEntity> userList = userRepository.findAll();
		
		inMemoryUserList.add(new JwtUserDetails(20000L, "temp",
				"$2a$10$KDLLS2LR6CN70N41MNRvLuE1pYytVd7S3Wf1qFYC8ToS71KLwHrhi", "ROLE_TEMP"));
		
		for(int i = 0; i < userList.size(); i++)
			addUser(userList.get(i).getUserName(), userList.get(i).getPassword());
    }

    public JwtUserDetails addUser(String username, String password)
	{
		if(inMemoryUserList.size() == 0)
			initialize();
		
		JwtUserDetails newUser = new JwtUserDetails(userCount, username, password, "ROLE_USER");

		if (!username.contentEquals(""))
		{
			inMemoryUserList.add(newUser);
			// Create User entity without manual ID - let Hibernate auto-generate it
			UserEntity user = new UserEntity();
			user.setUsername(username);
			user.setPassword(password);
			userRepository.save(user);
			userCount++;
		} else
			System.out.println("Did not create new user");

		return newUser;
	}

    @Override
	public JwtUserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		if(inMemoryUserList.size() == 0)
			initialize();
		
		Optional<JwtUserDetails> findFirst = inMemoryUserList.stream()
				.filter(user -> user.getUsername().equals(username)).findFirst();

		if (!findFirst.isPresent())
		{
			throw new UsernameNotFoundException(String.format("USER_NOT_FOUND '%s'.", username));
		}

		return findFirst.get();
	}

    public UserDetails loadUserById(Long id) throws UsernameNotFoundException
	{
		if(inMemoryUserList.size() == 0)
			initialize();
		
		Optional<JwtUserDetails> findFirst = inMemoryUserList.stream().filter(user -> user.getId().equals(id))
				.findFirst();

		if (!findFirst.isPresent())
		{
			throw new UsernameNotFoundException(String.format("USER_NOT_FOUND '%s'.", id));
		}

		return findFirst.get();
	}

    public boolean userExists(String username)
	{
		if(inMemoryUserList.size() == 0)
			initialize();
		
		Optional<JwtUserDetails> findFirst = inMemoryUserList.stream()
				.filter(user -> user.getUsername().equals(username)).findFirst();

		return findFirst.isPresent();
	}
    
}
