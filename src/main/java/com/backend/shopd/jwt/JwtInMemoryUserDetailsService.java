package com.backend.shopd.jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.backend.shopd.data.entity.UserEntity;
import com.backend.shopd.data.repository.UserRepository;
import com.backend.shopd.service.EmailService;
import org.springframework.security.authentication.DisabledException;

@Service
public class JwtInMemoryUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

	@Autowired
	private EmailService emailService;

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
			tempUserEntity.setVerified(true);
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

	public void addUser(String email, String username, String password) {
		String code = UUID.randomUUID().toString();
	
		UserEntity user = new UserEntity();
		user.setEmail(email);
		user.setUsername(username);
		user.setPassword(password);
		user.setAccountType("USER");
		user.setVerified(false);
		user.setVerificationCode(code);
		userRepository.save(user);
	
		// Also add to in-memory list
		inMemoryUserList.add(new JwtUserDetails(userCount++, username, password, "ROLE_USER"));
	
		emailService.sendVerificationEmail(email, code);
	}


    @Override
	public JwtUserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		if(!initialized)
			initialize();
		
		Optional<JwtUserDetails> findFirst = inMemoryUserList.stream()
				.filter(user -> user.getUsername().equals(username)).findFirst();

		if (!findFirst.isPresent())
		{
			// Fall back: treat the input as an email and resolve to a username
			Optional<UserEntity> userByEmail = userRepository.findByEmail(username);
			if (userByEmail.isPresent())
			{
				String resolvedUsername = userByEmail.get().getUsername();
				findFirst = inMemoryUserList.stream()
						.filter(user -> user.getUsername().equals(resolvedUsername)).findFirst();
			}
		}

		if (!findFirst.isPresent())
		{
			throw new UsernameNotFoundException(String.format("USER_NOT_FOUND '%s'.", username));
		}

		// Block login for unverified users
		Optional<UserEntity> dbUser = userRepository.findByUsername(findFirst.get().getUsername());
		if (dbUser.isPresent() && !dbUser.get().isVerified()) {
			throw new DisabledException(String.format("USER_NOT_VERIFIED '%s'.", findFirst.get().getUsername()));
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

    public String resolveToUsername(String usernameOrEmail)
	{
		if (!initialized)
			initialize();

		// Return as-is if it already matches a username in memory
		boolean isUsername = inMemoryUserList.stream()
				.anyMatch(user -> user.getUsername().equals(usernameOrEmail));
		if (isUsername)
			return usernameOrEmail;

		// Try resolving via email lookup
		return userRepository.findByEmail(usernameOrEmail)
				.map(UserEntity::getUsername)
				.orElse(usernameOrEmail);
	}
    
}
