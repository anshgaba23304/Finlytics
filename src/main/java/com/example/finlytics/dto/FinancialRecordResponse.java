package com.example.finlytics.dto;

import com.example.finlytics.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialRecordResponse(
		Long id,
		BigDecimal amount,
		TransactionType type,
		String category,
		LocalDate recordDate,
		String notes
) {
}
