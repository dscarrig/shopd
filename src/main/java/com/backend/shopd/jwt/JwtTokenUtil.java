package com.backend.shopd.jwt;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenUtil implements Serializable {

    static final String CLAIM_KEY_USERNAME = "sub";
	static final String CLAIM_KEY_CREATED = "iat";
	private static final long serialVersionUID = -3301605591108950415L;

	private final JwtConfigurationProperties jwtConfig;

	public JwtTokenUtil(JwtConfigurationProperties jwtConfig) {
		this.jwtConfig = jwtConfig;
	}

	public String getUsernameFromToken(String token)
	{
		return getClaimFromToken(token, Claims::getSubject);
	}

	public Date getIssuedAtDateFromToken(String token)
	{
		return getClaimFromToken(token, Claims::getIssuedAt);
	}

	public Date getExpirationDateFromToken(String token)
	{
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver)
	{
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	private Claims getAllClaimsFromToken(String token)
	{
		SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSigning().getKey().getSecret().getBytes());
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private Boolean isTokenExpired(String token)
	{
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	private Boolean ignoreTokenExpiration(String token)
	{
		// here you specify tokens, for that the expiration is ignored
		return false;
	}

	public String generateToken(UserDetails userDetails)
	{
		Map<String, Object> claims = new HashMap<>();
		return doGenerateToken(claims, userDetails.getUsername());
	}

	private String doGenerateToken(Map<String, Object> claims, String subject)
	{
		final Date createdDate = new Date();
		final Date expirationDate = calculateExpirationDate(createdDate);

		SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSigning().getKey().getSecret().getBytes());
		return Jwts.builder()
				.claims(claims)
				.subject(subject)
				.issuedAt(createdDate)
				.expiration(expirationDate)
				.signWith(key)
				.compact();
	}

	public Boolean canTokenBeRefreshed(String token)
	{
		return (!isTokenExpired(token) || ignoreTokenExpiration(token));
	}

	public String refreshToken(String token)
	{
		final Date createdDate = new Date();
		final Date expirationDate = calculateExpirationDate(createdDate);

		final Claims claims = getAllClaimsFromToken(token);
		
		SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSigning().getKey().getSecret().getBytes());
		return Jwts.builder()
				.claims(claims)
				.issuedAt(createdDate)
				.expiration(expirationDate)
				.signWith(key)
				.compact();
	}

	public Boolean validateToken(String token, UserDetails userDetails)
	{
		JwtUserDetails user = (JwtUserDetails) userDetails;
		final String username = getUsernameFromToken(token);
		return (username.equals(user.getUsername()) && !isTokenExpired(token));
	}

	private Date calculateExpirationDate(Date createdDate)
	{
		return new Date(createdDate.getTime() + jwtConfig.getToken().getExpiration().getIn().getSeconds() * 1000);
	}
    
}
