package com.example.finlytics.dto;

import java.math.BigDecimal;

public record TrendPointResponse(
		String period,
		BigDecimal income,
		BigDecimal expense
) {
}
