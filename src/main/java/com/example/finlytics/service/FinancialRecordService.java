package com.example.finlytics.service;

import com.example.finlytics.domain.FinancialRecord;
import com.example.finlytics.domain.TransactionType;
import com.example.finlytics.dto.FinancialRecordRequest;
import com.example.finlytics.dto.FinancialRecordResponse;
import com.example.finlytics.repository.FinancialRecordRepository;
import com.example.finlytics.repository.FinancialRecordSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class FinancialRecordService {

	private final FinancialRecordRepository repository;

	public FinancialRecordService(FinancialRecordRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public Page<FinancialRecordResponse> list(
			LocalDate from,
			LocalDate to,
			String category,
			TransactionType type,
			String search,
			Pageable pageable) {
		Specification<FinancialRecord> spec = Specification.allOf(
				FinancialRecordSpecifications.notDeleted(),
				FinancialRecordSpecifications.typeEquals(type),
				FinancialRecordSpecifications.categoryEquals(category),
				FinancialRecordSpecifications.recordDateOnOrAfter(from),
				FinancialRecordSpecifications.recordDateOnOrBefore(to),
				FinancialRecordSpecifications.searchTerm(search)
		);
		return repository.findAll(spec, pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public FinancialRecordResponse getById(Long id) {
		FinancialRecord r = repository.findById(id)
				.filter(f -> !f.isDeleted())
				.orElseThrow(() -> new NotFoundException("Financial record not found: " + id));
		return toResponse(r);
	}

	@Transactional
	public FinancialRecordResponse create(FinancialRecordRequest request) {
		FinancialRecord r = new FinancialRecord();
		apply(r, request);
		r = repository.save(r);
		return toResponse(r);
	}

	@Transactional
	public FinancialRecordResponse update(Long id, FinancialRecordRequest request) {
		FinancialRecord r = repository.findById(id)
				.filter(f -> !f.isDeleted())
				.orElseThrow(() -> new NotFoundException("Financial record not found: " + id));
		apply(r, request);
		r = repository.save(r);
		return toResponse(r);
	}

	@Transactional
	public void softDelete(Long id) {
		FinancialRecord r = repository.findById(id)
				.filter(f -> !f.isDeleted())
				.orElseThrow(() -> new NotFoundException("Financial record not found: " + id));
		r.setDeleted(true);
		repository.save(r);
	}

	private void apply(FinancialRecord r, FinancialRecordRequest request) {
		r.setAmount(request.amount());
		r.setType(request.type());
		r.setCategory(request.category().trim());
		r.setRecordDate(request.recordDate());
		r.setNotes(request.notes() != null ? request.notes().trim() : null);
	}

	private FinancialRecordResponse toResponse(FinancialRecord r) {
		return new FinancialRecordResponse(
				r.getId(),
				r.getAmount(),
				r.getType(),
				r.getCategory(),
				r.getRecordDate(),
				r.getNotes());
	}
}
