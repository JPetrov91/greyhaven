package com.example.game.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/**
	 * Placeholder until Task 2 replaces this with a persistent account-backed
	 * {@link UserDetailsService}. An empty manager disables Boot's generated
	 * default user while keeping Security on the classpath.
	 */
	@Bean
	UserDetailsService userDetailsService() {
		return new InMemoryUserDetailsManager();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// SPA-appropriate CSRF: HttpOnly=false cookie + deferred-token load on each request.
				.csrf(csrf -> csrf.spa())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
						.requestMatchers("/api/v1/bootstrap").permitAll()
						.anyRequest().denyAll())
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable);

		return http.build();
	}
}
