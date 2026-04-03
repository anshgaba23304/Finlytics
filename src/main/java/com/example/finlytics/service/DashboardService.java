package com.example.finlytics.service;

import com.example.finlytics.domain.TransactionType;
import com.example.finlytics.dto.DashboardSummaryResponse;
import com.example.finlytics.dto.FinancialRecordResponse;
import com.example.finlytics.dto.RecentActivityResponse;
import com.example.finlytics.dto.TrendPointResponse;
import com.example.finlytics.repository.FinancialRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

	private final FinancialRecordRepository repository;

	public DashboardService(FinancialRecordRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public DashboardSummaryResponse summary() {
		BigDecimal income = nullToZero(repository.sumAmountByType(TransactionType.INCOME));
		BigDecimal expense = nullToZero(repository.sumAmountByType(TransactionType.EXPENSE));
		BigDecimal net = income.subtract(expense);
		List<DashboardSummaryResponse.CategoryTotalDto> incomeByCat = repository.sumAmountGroupedByCategory(TransactionType.INCOME)
				.stream()
				.map(row -> new DashboardSummaryResponse.CategoryTotalDto((String) row[0], (BigDecimal) row[1]))
				.toList();
		List<DashboardSummaryResponse.CategoryTotalDto> expenseByCat = repository.sumAmountGroupedByCategory(TransactionType.EXPENSE)
				.stream()
				.map(row -> new DashboardSummaryResponse.CategoryTotalDto((String) row[0], (BigDecimal) row[1]))
				.toList();
		return new DashboardSummaryResponse(income, expense, net, incomeByCat, expenseByCat);
	}

	@Transactional(readOnly = true)
	public List<TrendPointResponse> trends(String granularity) {
		String g = granularity == null ? "monthly" : granularity.trim().toLowerCase();
		TransactionType income = TransactionType.INCOME;
		TransactionType expense = TransactionType.EXPENSE;
		List<Object[]> rows = switch (g) {
			case "weekly" -> repository.weeklyTrendsRaw(income, expense);
			case "monthly" -> repository.monthlyTrendsRaw(income, expense);
			default -> throw new IllegalArgumentException("granularity must be 'monthly' or 'weekly'");
		};
		return rows.stream()
				.map(row -> switch (g) {
					case "weekly" -> new TrendPointResponse(
							formatWeeklyPeriod(row[0], row[1]),
							toBigDecimal(row[2]),
							toBigDecimal(row[3]));
					default -> new TrendPointResponse(
							formatMonthlyPeriod(row[0], row[1]),
							toBigDecimal(row[2]),
							toBigDecimal(row[3]));
				})
				.toList();
	}

	private static String formatMonthlyPeriod(Object yearPart, Object monthPart) {
		int y = ((Number) yearPart).intValue();
		int m = ((Number) monthPart).intValue();
		return String.format("%d-%02d", y, m);
	}

	private static String formatWeeklyPeriod(Object yearPart, Object weekPart) {
		int y = ((Number) yearPart).intValue();
		int w = ((Number) weekPart).intValue();
		return y + "-W" + String.format("%02d", w);
	}

	@Transactional(readOnly = true)
	public RecentActivityResponse recent(int limit) {
		int safe = Math.min(Math.max(limit, 1), 100);
		var page = repository.findByDeletedFalseOrderByRecordDateDescIdDesc(
				PageRequest.of(0, safe, Sort.by(Sort.Direction.DESC, "recordDate", "id")));
		List<FinancialRecordResponse> items = page.getContent().stream()
				.map(r -> new FinancialRecordResponse(
						r.getId(),
						r.getAmount(),
						r.getType(),
						r.getCategory(),
						r.getRecordDate(),
						r.getNotes()))
				.toList();
		return new RecentActivityResponse(items);
	}

	private static BigDecimal nullToZero(BigDecimal v) {
		return v != null ? v : BigDecimal.ZERO;
	}

	private static BigDecimal toBigDecimal(Object o) {
		if (o == null) {
			return BigDecimal.ZERO;
		}
		if (o instanceof BigDecimal bd) {
			return bd;
		}
		if (o instanceof Number n) {
			return BigDecimal.valueOf(n.doubleValue());
		}
		return new BigDecimal(o.toString());
	}
}
