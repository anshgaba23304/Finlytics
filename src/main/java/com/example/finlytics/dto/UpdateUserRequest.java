package com.example.finlytics.dto;

import com.example.finlytics.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
		@Email String email,
		Role role,
		Boolean active
) {
}
