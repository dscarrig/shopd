package com.backend.shopd.jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
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

    private List<JwtUserDetails> inMemoryUserList = new ArrayList<>();

    private long userCount;
    private boolean initialized = false;

    public JwtInMemoryUserDetailsService()
	{
		userCount = 20001;
	}

    @PostConstruct
    public void initializeOnStartup() {
        initialize();
    }

    public void initialize() {
        // Only initialize once per service instance
        if (initialized) {
            return;
        }
        System.out.println("Initializing in-memory user details from database...");
        inMemoryUserList.clear();
        List<UserEntity> userList = userRepository.findAll();
		
		JwtUserDetails tempUser = new JwtUserDetails(20000L, "temp",
				"$2a$10$KDLLS2LR6CN70N41MNRvLuE1pYytVd7S3Wf1qFYC8ToS71KLwHrhi", "ROLE_TEMP");
		inMemoryUserList.add(tempUser);
		System.out.println("JwtUserDetails - created user details for: " + tempUser.getUsername());
		
		// Also save temp user to repository if it doesn't exist
		if (userRepository.findByUsername("temp").isEmpty()) {
			UserEntity tempUserEntity = new UserEntity();
			tempUserEntity.setUsername("temp");
			tempUserEntity.setPassword("$2a$10$KDLLS2LR6CN70N41MNRvLuE1pYytVd7S3Wf1qFYC8ToS71KLwHrhi");
			tempUserEntity.setEmail("temp@shopd.local");
			tempUserEntity.setAccountType("TEMP");
			userRepository.save(tempUserEntity);
			System.out.println("Saved temp user to repository");
		}
		
		for(int i = 0; i < userList.size(); i++) {
			// Load existing users into memory WITHOUT saving to database again
			JwtUserDetails existingUser = new JwtUserDetails(userCount, userList.get(i).getUsername(), userList.get(i).getPassword(), "ROLE_USER");
			inMemoryUserList.add(existingUser);
			System.out.println("Added existing user to memory: " + userList.get(i).getUsername());
			userCount++;
		}
		initialized = true;
    }

    public JwtUserDetails addUser(String username, String password)
	{
		if(!initialized)
			initialize();
		
		JwtUserDetails newUser = new JwtUserDetails(userCount, username, password, "ROLE_USER");

		if (!username.contentEquals(""))
		{
			inMemoryUserList.add(newUser);
			// Create User entity without manual ID - let Hibernate auto-generate it
			UserEntity user = new UserEntity();
			user.setUsername(username);
			user.setPassword(password);
			user.setEmail(username + "@shopd.local"); // Default email if not provided
			user.setAccountType("USER"); // Set default account type
			userRepository.save(user);
			userCount++;
		} else
			System.out.println("Did not create new user");

		return newUser;
	}

    @Override
	public JwtUserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		if(!initialized)
			initialize();
		
		System.out.println("Looking for user: " + username + ". In-memory users: " + inMemoryUserList.size());
		for(JwtUserDetails user : inMemoryUserList) {
			System.out.println("  - Available user: " + user.getUsername());
		}
		
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
