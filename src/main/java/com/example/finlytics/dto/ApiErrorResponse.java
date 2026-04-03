package com.example.finlytics.dto;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
		int status,
		String error,
		String message,
		List<String> details,
		String path,
		Instant timestamp
) {
}
