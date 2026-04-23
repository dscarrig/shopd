package com.backend.shopd.jwt;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtTokenUtil implements Serializable {

    static final String CLAIM_KEY_USERNAME = "sub";
	static final String CLAIM_KEY_CREATED = "iat";
	private static final long serialVersionUID = -3301605591108950415L;

	private final JwtConfigurationProperties jwtConfig;
	private PrivateKey privateKey;
	private PublicKey publicKey;

	public JwtTokenUtil(JwtConfigurationProperties jwtConfig) {
		this.jwtConfig = jwtConfig;
		loadKeys();
	}

	private void loadKeys() {
		try {
			String privatePath = jwtConfig.getSigning().getKey().getPrivateKeyPath();
			String publicPath = jwtConfig.getSigning().getKey().getPublicKeyPath();
			privateKey = loadPrivateKey(privatePath);
			publicKey = loadPublicKey(publicPath);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to load JWT RSA keys from PEM files", e);
		}
	}

	private PrivateKey loadPrivateKey(String path) throws Exception {
		String pem = new String(Files.readAllBytes(Paths.get(path)));
		String keyContent = pem
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s", "");
		byte[] keyBytes = Base64.getDecoder().decode(keyContent);
		return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
	}

	private PublicKey loadPublicKey(String path) throws Exception {
		String pem = new String(Files.readAllBytes(Paths.get(path)));
		String keyContent = pem
				.replace("-----BEGIN PUBLIC KEY-----", "")
				.replace("-----END PUBLIC KEY-----", "")
				.replaceAll("\\s", "");
		byte[] keyBytes = Base64.getDecoder().decode(keyContent);
		return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
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
		return Jwts.parser()
				.verifyWith(publicKey)
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

		return Jwts.builder()
				.claims(claims)
				.subject(subject)
				.issuedAt(createdDate)
				.expiration(expirationDate)
				.signWith(privateKey)
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

		return Jwts.builder()
				.claims(claims)
				.issuedAt(createdDate)
				.expiration(expirationDate)
				.signWith(privateKey)
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

