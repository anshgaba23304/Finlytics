package com.example.finlytics.service;

import com.example.finlytics.dto.LoginRequest;
import com.example.finlytics.dto.TokenResponse;
import com.example.finlytics.security.JwtService;
import com.example.finlytics.security.SecurityUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	@Transactional(readOnly = true)
	public TokenResponse login(LoginRequest request) {
		try {
			Authentication auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.username(), request.password()));
			SecurityUser principal = (SecurityUser) auth.getPrincipal();
			String token = jwtService.generateToken(principal.getUsername(), principal.getRole());
			return new TokenResponse(token, "Bearer", jwtService.getExpirationMs() / 1000);
		} catch (Exception ex) {
			throw new BadCredentialsException("Invalid username or password", ex);
		}
	}
}
