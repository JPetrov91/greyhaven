package com.example.game.account.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.account.domain.EmailAddress;
import com.example.game.account.infrastructure.AccountEntity;
import com.example.game.account.infrastructure.AccountRepository;
import com.example.game.shared.api.ApiException;
import com.example.game.shared.infrastructure.ConstraintViolations;

@Service
public class AuthApplicationService {

	private static final String UNIQUE_EMAIL_CONSTRAINT = "uq_accounts_email_lower";

	private final AccountRepository accountRepository;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	public AuthApplicationService(
			AccountRepository accountRepository,
			PasswordEncoder passwordEncoder,
			Clock clock) {
		this.accountRepository = accountRepository;
		this.passwordEncoder = passwordEncoder;
		this.clock = clock;
	}

	@Transactional
	public AccountView register(String email, String rawPassword) {
		String normalizedEmail = EmailAddress.normalize(email);
		if (accountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
			throw emailAlreadyExists();
		}

		Instant now = Instant.now(clock);
		AccountEntity account = new AccountEntity(
				UUID.randomUUID(),
				normalizedEmail,
				passwordEncoder.encode(rawPassword),
				now,
				now);

		try {
			accountRepository.saveAndFlush(account);
		}
		catch (DataIntegrityViolationException exception) {
			if (ConstraintViolations.caused(exception, UNIQUE_EMAIL_CONSTRAINT)) {
				throw emailAlreadyExists();
			}
			throw exception;
		}

		return toView(account);
	}

	@Transactional(readOnly = true)
	public AccountView currentUser(UUID accountId) {
		AccountEntity account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ApiException("ACCOUNT_NOT_FOUND", "Account not found.", HttpStatus.NOT_FOUND));
		return toView(account);
	}

	private static AccountView toView(AccountEntity account) {
		return new AccountView(account.getId(), account.getEmail());
	}

	private static ApiException emailAlreadyExists() {
		return new ApiException(
				"EMAIL_ALREADY_EXISTS",
				"An account with this email already exists.",
				HttpStatus.CONFLICT);
	}
}
