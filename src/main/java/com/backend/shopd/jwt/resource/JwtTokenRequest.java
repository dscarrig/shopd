package com.backend.shopd.jwt.resource;

import java.io.Serializable;

public class JwtTokenRequest implements Serializable
{

	private static final long serialVersionUID = -5616176897013108345L;

	private String email;
	private String username;
	private String password;

	public JwtTokenRequest()
	{
		super();
	}

	public JwtTokenRequest(String email, String username, String password)
	{
		this.setEmail(email);
		this.setUsername(username);
		this.setPassword(password);
	}

	public String getEmail()
	{
		return this.email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getUsername()
	{
		return this.username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return this.password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
}