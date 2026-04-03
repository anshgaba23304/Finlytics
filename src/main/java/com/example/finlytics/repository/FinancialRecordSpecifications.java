package com.example.finlytics.repository;

import com.example.finlytics.domain.FinancialRecord;
import com.example.finlytics.domain.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class FinancialRecordSpecifications {

	private FinancialRecordSpecifications() {
	}

	public static Specification<FinancialRecord> notDeleted() {
		return (root, query, cb) -> cb.isFalse(root.get("deleted"));
	}

	public static Specification<FinancialRecord> typeEquals(TransactionType type) {
		if (type == null) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.equal(root.get("type"), type);
	}

	public static Specification<FinancialRecord> categoryEquals(String category) {
		if (category == null || category.isBlank()) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase());
	}

	public static Specification<FinancialRecord> recordDateOnOrAfter(LocalDate from) {
		if (from == null) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("recordDate"), from);
	}

	public static Specification<FinancialRecord> recordDateOnOrBefore(LocalDate to) {
		if (to == null) {
			return (root, query, cb) -> cb.conjunction();
		}
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("recordDate"), to);
	}

	public static Specification<FinancialRecord> searchTerm(String q) {
		if (q == null || q.isBlank()) {
			return (root, query, cb) -> cb.conjunction();
		}
		String pattern = "%" + q.trim().toLowerCase() + "%";
		return (root, query, cb) -> cb.or(
				cb.like(cb.lower(root.get("notes")), pattern),
				cb.like(cb.lower(root.get("category")), pattern));
	}
}
