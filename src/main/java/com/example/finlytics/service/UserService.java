package com.example.finlytics.service;

import com.example.finlytics.domain.User;
import com.example.finlytics.dto.ChangePasswordRequest;
import com.example.finlytics.dto.CreateUserRequest;
import com.example.finlytics.dto.UpdateUserRequest;
import com.example.finlytics.dto.UserResponse;
import com.example.finlytics.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> listAll() {
		return userRepository.findAll(Sort.by("id")).stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public UserResponse getById(Long id) {
		User u = userRepository.findById(id).orElseThrow(() -> new com.example.finlytics.service.NotFoundException("User not found: " + id));
		return toResponse(u);
	}

	@Transactional
	public UserResponse create(CreateUserRequest request) {
		if (userRepository.existsByUsername(request.username())) {
			throw new ConflictException("Username already exists: " + request.username());
		}
		if (userRepository.existsByEmail(request.email())) {
			throw new ConflictException("Email already exists: " + request.email());
		}
		User u = new User();
		u.setUsername(request.username().trim());
		u.setEmail(request.email().trim().toLowerCase());
		u.setPasswordHash(passwordEncoder.encode(request.password()));
		u.setRole(request.role());
		u.setActive(true);
		u = userRepository.save(u);
		return toResponse(u);
	}

	@Transactional
	public UserResponse update(Long id, UpdateUserRequest request) {
		User u = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found: " + id));
		if (request.email() != null) {
			String email = request.email().trim().toLowerCase();
			if (!email.equals(u.getEmail()) && userRepository.existsByEmail(email)) {
				throw new ConflictException("Email already exists: " + email);
			}
			u.setEmail(email);
		}
		if (request.role() != null) {
			u.setRole(request.role());
		}
		if (request.active() != null) {
			u.setActive(request.active());
		}
		u = userRepository.save(u);
		return toResponse(u);
	}

	@Transactional
	public void delete(Long id) {
		if (!userRepository.existsById(id)) {
			throw new NotFoundException("User not found: " + id);
		}
		userRepository.deleteById(id);
	}

	@Transactional
	public void changeOwnPassword(String username, ChangePasswordRequest request) {
		User u = userRepository.findByUsername(username)
				.orElseThrow(() -> new NotFoundException("User not found: " + username));
		if (!passwordEncoder.matches(request.currentPassword(), u.getPasswordHash())) {
			throw new IllegalArgumentException("Current password is incorrect");
		}
		if (request.currentPassword().equals(request.newPassword())) {
			throw new IllegalArgumentException("New password must be different from the current password");
		}
		u.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		userRepository.save(u);
	}

	private UserResponse toResponse(User u) {
		return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole(), u.isActive(), u.getCreatedAt());
	}
}
