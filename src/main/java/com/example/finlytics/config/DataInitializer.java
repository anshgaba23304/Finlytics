package com.example.finlytics.config;

import com.example.finlytics.domain.FinancialRecord;
import com.example.finlytics.domain.Role;
import com.example.finlytics.domain.TransactionType;
import com.example.finlytics.domain.User;
import com.example.finlytics.repository.FinancialRecordRepository;
import com.example.finlytics.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataInitializer {

	@Bean
	@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
	CommandLineRunner seedUsers(UserRepository users, FinancialRecordRepository records, PasswordEncoder encoder) {
		return args -> {
			if (users.count() > 0) {
				return;
			}
			User admin = new User();
			admin.setUsername("admin");
			admin.setEmail("admin@finlytics.local");
			admin.setPasswordHash(encoder.encode("Admin123!"));
			admin.setRole(Role.ADMIN);
			admin.setActive(true);
			users.save(admin);

			User analyst = new User();
			analyst.setUsername("analyst");
			analyst.setEmail("analyst@finlytics.local");
			analyst.setPasswordHash(encoder.encode("Analyst123!"));
			analyst.setRole(Role.ANALYST);
			analyst.setActive(true);
			users.save(analyst);

			User viewer = new User();
			viewer.setUsername("viewer");
			viewer.setEmail("viewer@finlytics.local");
			viewer.setPasswordHash(encoder.encode("Viewer123!"));
			viewer.setRole(Role.VIEWER);
			viewer.setActive(true);
			users.save(viewer);

			addRecord(records, "1000.00", TransactionType.INCOME, "Salary", LocalDate.now().minusMonths(1), "Monthly salary");
			addRecord(records, "250.50", TransactionType.EXPENSE, "Utilities", LocalDate.now().minusWeeks(2), "Electricity");
			addRecord(records, "89.99", TransactionType.EXPENSE, "Software", LocalDate.now().minusDays(5), "Subscription");
			addRecord(records, "500.00", TransactionType.INCOME, "Freelance", LocalDate.now().minusDays(10), "Project payment");
			addRecord(records, "1200.00", TransactionType.EXPENSE, "Rent", LocalDate.now().withDayOfMonth(1), "Office rent");
		};
	}

	private static void addRecord(
			FinancialRecordRepository records,
			String amount,
			TransactionType type,
			String category,
			LocalDate date,
			String notes) {
		FinancialRecord r = new FinancialRecord();
		r.setAmount(new BigDecimal(amount));
		r.setType(type);
		r.setCategory(category);
		r.setRecordDate(date);
		r.setNotes(notes);
		r.setDeleted(false);
		records.save(r);
	}
}
