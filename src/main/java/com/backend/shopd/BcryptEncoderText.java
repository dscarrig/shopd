package com.backend.shopd;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptEncoderText
{

	public static void main(String[] args)
	{
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		String encodedString = encoder.encode("temp");
		System.out.println(encodedString);
	}

}