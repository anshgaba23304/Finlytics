package com.example.finlytics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Fails fast in production if JWT secret was left at the development default.
 */
@Component
@Profile("prod")
@Order(0)
public class ProductionSecurityValidator implements ApplicationRunner {

	private static final String DEV_PLACEHOLDER =
			"ChangeThisToAVeryLongSecretKeyForHS256AlgorithmMinimum256BitsRequiredForProductionUseOnly";

	private final String jwtSecret;

	public ProductionSecurityValidator(@Value("${app.security.jwt.secret}") String jwtSecret) {
		this.jwtSecret = jwtSecret;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (jwtSecret == null || jwtSecret.isBlank() || DEV_PLACEHOLDER.equals(jwtSecret)) {
			throw new IllegalStateException(
					"Production requires a strong APP_JWT_SECRET (not the default placeholder).");
		}
	}
}
