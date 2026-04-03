package com.example.finlytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
		@NotBlank(message = "currentPassword is required") String currentPassword,
		@NotBlank @Size(min = 8, max = 128, message = "newPassword must be at least 8 characters") String newPassword
) {
}
