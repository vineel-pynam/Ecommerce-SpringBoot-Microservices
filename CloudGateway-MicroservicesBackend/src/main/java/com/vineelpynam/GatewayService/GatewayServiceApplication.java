package com.vineelpynam.GatewayService;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import java.time.Duration;

@SpringBootApplication
public class GatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

	@Bean
	public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer(){
		return factory -> factory.configureDefault(
				id -> new Resilience4JConfigBuilder(id)
						.circuitBreakerConfig(
								CircuitBreakerConfig.ofDefaults()
						)
						.timeLimiterConfig(TimeLimiterConfig
								.custom()
								.timeoutDuration(Duration.ofSeconds(60)).build())
						.build()
		);
	}

	@Bean
	KeyResolver authUserKeyResolver() {
		return exchange -> ReactiveSecurityContextHolder.getContext()
				.map(ctx -> ctx.getAuthentication()
						.getCredentials().toString());
	}
}
