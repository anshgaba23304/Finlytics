package com.example.finlytics.controller;

import com.example.finlytics.dto.DashboardSummaryResponse;
import com.example.finlytics.dto.RecentActivityResponse;
import com.example.finlytics.dto.TrendPointResponse;
import com.example.finlytics.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/summary")
	@PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
	public ResponseEntity<DashboardSummaryResponse> summary() {
		return ResponseEntity.ok(dashboardService.summary());
	}

	@GetMapping("/trends")
	@PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
	public ResponseEntity<List<TrendPointResponse>> trends(
			@RequestParam(defaultValue = "monthly") String granularity) {
		return ResponseEntity.ok(dashboardService.trends(granularity));
	}

	@GetMapping("/recent")
	@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
	public ResponseEntity<RecentActivityResponse> recent(@RequestParam(defaultValue = "10") int limit) {
		return ResponseEntity.ok(dashboardService.recent(limit));
	}
}
