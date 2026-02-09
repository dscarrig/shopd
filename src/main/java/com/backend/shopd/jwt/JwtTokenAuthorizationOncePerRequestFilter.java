package com.backend.shopd.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenAuthorizationOncePerRequestFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private JwtInMemoryUserDetailsService jwtInMemoryUserDetailsService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Value("${jwt.http.request.header}")
	private String tokenHeader;

	@SuppressWarnings("null")
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException
	{
		logger.debug("Authentication Request For '{}'", request.getRequestURL());

		// Skip JWT processing for public endpoints
		String requestPath = request.getRequestURI();
		if (requestPath.equals("/register") || requestPath.equals("/authenticate") || 
		    requestPath.startsWith("/users/new/") || requestPath.startsWith("/users/exists/") ||
		    requestPath.equals("/") || requestPath.equals("/login") || requestPath.equals("/index") || 
		    requestPath.equals("/create-user") || requestPath.equals("/items") || requestPath.equals("/api/items")) {
			chain.doFilter(request, response);
			return;
		}

		final String requestTokenHeader = request.getHeader(this.tokenHeader);

		String username = null;
		String jwtToken = null;
		if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer "))
		{
			jwtToken = requestTokenHeader.substring(7);
			try
			{
				username = jwtTokenUtil.getUsernameFromToken(jwtToken);
			} catch (IllegalArgumentException e)
			{
				logger.error("JWT_TOKEN_UNABLE_TO_GET_USERNAME", e);
			}
            catch (ExpiredJwtException e)
			{
				logger.warn("JWT_TOKEN_EXPIRED", e);
			}
		}
		else
		{
			logger.warn("JWT_TOKEN_DOES_NOT_BEGIN_WITH_BEARER_STRING");
		}
		logger.debug("JWT_TOKEN_USERNAME_VALUE '{}'", username);
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null)
		{

			UserDetails userDetails = this.jwtInMemoryUserDetailsService.loadUserByUsername(username);

			if (jwtTokenUtil.validateToken(jwtToken, userDetails))
			{
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			}
		}

		chain.doFilter(request, response);
	}
}
