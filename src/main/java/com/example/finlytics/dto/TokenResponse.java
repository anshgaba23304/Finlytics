package com.example.finlytics.dto;

public record TokenResponse(
		String accessToken,
		String tokenType,
		long expiresInSeconds
) {
}
