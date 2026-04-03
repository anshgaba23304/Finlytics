package com.example.finlytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
		BigDecimal totalIncome,
		BigDecimal totalExpense,
		BigDecimal netBalance,
		List<CategoryTotalDto> incomeByCategory,
		List<CategoryTotalDto> expenseByCategory
) {
	public record CategoryTotalDto(String category, BigDecimal total) {
	}
}
