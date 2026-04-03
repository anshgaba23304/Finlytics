package com.example.finlytics.exception;

import com.example.finlytics.dto.ApiErrorResponse;
import com.example.finlytics.service.ConflictException;
import com.example.finlytics.service.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiErrorResponse> notFound(NotFoundException ex, HttpServletRequest req) {
		return error(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), null, req);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiErrorResponse> conflict(ConflictException ex, HttpServletRequest req) {
		return error(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), null, req);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> badRequest(IllegalArgumentException ex, HttpServletRequest req) {
		return error(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), null, req);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		List<String> detailList = ex.getBindingResult().getFieldErrors().stream()
				.map(GlobalExceptionHandler::formatFieldError)
				.collect(Collectors.toList());
		return errorList(HttpStatus.BAD_REQUEST, "Validation Failed", "Invalid request body", detailList, req);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> badCreds(BadCredentialsException ex, HttpServletRequest req) {
		return error(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid username or password", null, req);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> forbidden(AccessDeniedException ex, HttpServletRequest req) {
		return error(HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission for this action", null, req);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> fallback(Exception ex, HttpServletRequest req) {
		log.error("Unhandled error", ex);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", null, req);
	}

	private static String formatFieldError(FieldError fe) {
		return fe.getField() + ": " + fe.getDefaultMessage();
	}

	private static ResponseEntity<ApiErrorResponse> error(
			HttpStatus status,
			String error,
			String message,
			String details,
			HttpServletRequest req) {
		List<String> list = details != null ? List.of(details) : List.of();
		return errorList(status, error, message, list, req);
	}

	private static ResponseEntity<ApiErrorResponse> errorList(
			HttpStatus status,
			String error,
			String message,
			List<String> details,
			HttpServletRequest req) {
		ApiErrorResponse body = new ApiErrorResponse(
				status.value(),
				error,
				message,
				details,
				req.getRequestURI(),
				Instant.now());
		return ResponseEntity.status(status).body(body);
	}
}
