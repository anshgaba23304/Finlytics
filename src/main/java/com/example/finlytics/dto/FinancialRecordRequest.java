package com.example.finlytics.dto;

import com.example.finlytics.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialRecordRequest(
		@NotNull @DecimalMin(value = "0.0001", inclusive = true, message = "amount must be positive") BigDecimal amount,
		@NotNull TransactionType type,
		@NotBlank @Size(max = 120) String category,
		@NotNull LocalDate recordDate,
		@Size(max = 2000) String notes
) {
}
