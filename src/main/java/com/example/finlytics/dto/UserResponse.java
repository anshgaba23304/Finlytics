package com.example.finlytics.dto;

import com.example.finlytics.domain.Role;

import java.time.Instant;

public record UserResponse(
		Long id,
		String username,
		String email,
		Role role,
		boolean active,
		Instant createdAt
) {
}
