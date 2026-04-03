package com.example.finlytics.controller;

import com.example.finlytics.domain.TransactionType;
import com.example.finlytics.dto.FinancialRecordRequest;
import com.example.finlytics.dto.FinancialRecordResponse;
import com.example.finlytics.service.FinancialRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
public class FinancialRecordController {

	private final FinancialRecordService recordService;

	public FinancialRecordController(FinancialRecordService recordService) {
		this.recordService = recordService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
	public ResponseEntity<Page<FinancialRecordResponse>> list(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) TransactionType type,
			@RequestParam(required = false) String q,
			@PageableDefault(size = 20, sort = "recordDate", direction = Sort.Direction.DESC) Pageable pageable) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new IllegalArgumentException("'from' must be on or before 'to'");
		}
		return ResponseEntity.ok(recordService.list(from, to, category, type, q, pageable));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
	public ResponseEntity<FinancialRecordResponse> get(@PathVariable Long id) {
		return ResponseEntity.ok(recordService.getById(id));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<FinancialRecordResponse> create(@Valid @RequestBody FinancialRecordRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(recordService.create(request));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<FinancialRecordResponse> update(
			@PathVariable Long id,
			@Valid @RequestBody FinancialRecordRequest request) {
		return ResponseEntity.ok(recordService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		recordService.softDelete(id);
		return ResponseEntity.noContent().build();
	}
}
