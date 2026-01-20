package com.backend.shopd.web.controller;

import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.backend.shopd.jwt.JwtInMemoryUserDetailsService;
import com.backend.shopd.jwt.JwtTokenUtil;
import com.backend.shopd.jwt.JwtUserDetails;
import com.backend.shopd.jwt.resource.AuthenticationException;
import com.backend.shopd.jwt.resource.ErrorResponse;
import com.backend.shopd.jwt.resource.JwtTokenRequest;
import com.backend.shopd.jwt.resource.JwtTokenResponse;

@RestController
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8081"})
public class JwtAuthenticationRestController
{

	@Value("${jwt.http.request.header}")
	private String tokenHeader;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JwtInMemoryUserDetailsService jwtInMemoryUserDetailsService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@RequestMapping(value = "${jwt.get.token.uri}", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtTokenRequest authenticationRequest)
	{
		System.out.println("JwtAuthenticationRestController - createAuthenticationToken for: "
				+ authenticationRequest.getUsername());

		authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

		final UserDetails userDetails = jwtInMemoryUserDetailsService
				.loadUserByUsername(authenticationRequest.getUsername());

		final String token = jwtTokenUtil.generateToken(userDetails);

		return ResponseEntity.ok(new JwtTokenResponse(token));
	}

	@RequestMapping(value = "${jwt.refresh.token.uri}", method = RequestMethod.GET)
	public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request)
	{
		String authToken = request.getHeader(tokenHeader);
		final String token = authToken.substring(7);
		String username = jwtTokenUtil.getUsernameFromToken(token);
		@SuppressWarnings("unused")
		JwtUserDetails user = (JwtUserDetails) jwtInMemoryUserDetailsService.loadUserByUsername(username);

		if (jwtTokenUtil.canTokenBeRefreshed(token))
		{
			String refreshedToken = jwtTokenUtil.refreshToken(token);
			return ResponseEntity.ok(new JwtTokenResponse(refreshedToken));
		} else
		{
			return ResponseEntity.badRequest().body(null);
		}
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ResponseEntity<?> registerUser(@RequestBody JwtTokenRequest registrationRequest)
	{
		System.out.println("JwtAuthenticationRestController - registerUser for: "
				+ registrationRequest.getUsername());

		// Check if user already exists
		if (jwtInMemoryUserDetailsService.userExists(registrationRequest.getUsername()))
		{
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
					.body(new ErrorResponse("Username already exists"));
		}

		// Hash the password before storing
		String hashedPassword = passwordEncoder.encode(registrationRequest.getPassword());

		// Add user to the system
		jwtInMemoryUserDetailsService.addUser(registrationRequest.getUsername(), hashedPassword);

		System.out.println("JwtAuthenticationRestController - user registered successfully: "
				+ registrationRequest.getUsername());

		return ResponseEntity.status(HttpStatus.CREATED)
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.body(new ErrorResponse("User registered successfully"));
	}

	@ExceptionHandler({ AuthenticationException.class })
	public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e)
	{
		System.out.println("JwtAuthenticationRestController - handleAuthenticationException: " + e.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
		System.out.println("ErrorResponse created with message: " + errorResponse.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
			.body(errorResponse);
	}

	private void authenticate(String username, String password)
	{
		System.out.println("JwtAuthenticationRestController - authenticate user: " + username);

		Objects.requireNonNull(username);
		Objects.requireNonNull(password);

		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
			System.out.println("JwtAuthenticationRestController - authenticate successful for user: " + username);
		} catch (DisabledException e) {
			System.out.println("JwtAuthenticationRestController - authenticate failed - user disabled: " + username);
			throw new AuthenticationException("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			System.out.println("JwtAuthenticationRestController - authenticate failed - bad credentials: " + username);
			throw new AuthenticationException("INVALID_CREDENTIALS", e);
		}
	}
}