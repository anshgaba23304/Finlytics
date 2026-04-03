package com.example.finlytics.repository;

import com.example.finlytics.domain.FinancialRecord;
import com.example.finlytics.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>,
		JpaSpecificationExecutor<FinancialRecord> {

	@Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialRecord f WHERE f.deleted = false AND f.type = :type")
	BigDecimal sumAmountByType(@Param("type") TransactionType type);

	@Query("SELECT f.category, COALESCE(SUM(f.amount), 0) FROM FinancialRecord f WHERE f.deleted = false AND f.type = :type GROUP BY f.category ORDER BY f.category")
	List<Object[]> sumAmountGroupedByCategory(@Param("type") TransactionType type);

	/**
	 * Portable JPQL (PostgreSQL in prod, H2 in tests). Rows: year, month, income sum, expense sum.
	 */
	@Query("""
			SELECT YEAR(f.recordDate), MONTH(f.recordDate),
			       COALESCE(SUM(CASE WHEN f.type = :income THEN f.amount ELSE 0 END), 0),
			       COALESCE(SUM(CASE WHEN f.type = :expense THEN f.amount ELSE 0 END), 0)
			FROM FinancialRecord f
			WHERE f.deleted = false
			GROUP BY YEAR(f.recordDate), MONTH(f.recordDate)
			ORDER BY YEAR(f.recordDate), MONTH(f.recordDate)
			""")
	List<Object[]> monthlyTrendsRaw(
			@Param("income") TransactionType income,
			@Param("expense") TransactionType expense);

	/**
	 * Portable JPQL. Rows: calendar year, week-of-year, income sum, expense sum.
	 * Week semantics follow the JDBC/Hibernate dialect (H2 vs PostgreSQL may differ slightly).
	 */
	@Query("""
			SELECT YEAR(f.recordDate), WEEK(f.recordDate),
			       COALESCE(SUM(CASE WHEN f.type = :income THEN f.amount ELSE 0 END), 0),
			       COALESCE(SUM(CASE WHEN f.type = :expense THEN f.amount ELSE 0 END), 0)
			FROM FinancialRecord f
			WHERE f.deleted = false
			GROUP BY YEAR(f.recordDate), WEEK(f.recordDate)
			ORDER BY YEAR(f.recordDate), WEEK(f.recordDate)
			""")
	List<Object[]> weeklyTrendsRaw(
			@Param("income") TransactionType income,
			@Param("expense") TransactionType expense);

	Page<FinancialRecord> findByDeletedFalseOrderByRecordDateDescIdDesc(Pageable pageable);
}
