package com.example.finlytics.dto;

import java.util.List;

public record RecentActivityResponse(
		List<FinancialRecordResponse> items
) {
}
