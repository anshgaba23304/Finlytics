package com.example.finlytics.dto;

import com.example.finlytics.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
		@NotBlank @Size(min = 3, max = 80) String username,
		@NotBlank @Email String email,
		@NotBlank @Size(min = 8, max = 128) String password,
		@NotNull Role role
) {
}
