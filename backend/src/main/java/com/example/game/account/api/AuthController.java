package com.example.game.account.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.application.AccountView;
import com.example.game.account.application.AuthApplicationService;
import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.account.infrastructure.SessionAuthenticator;
import com.example.game.character.application.CharacterApplicationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

	private final AuthApplicationService authApplicationService;
	private final CharacterApplicationService characterApplicationService;
	private final SessionAuthenticator sessionAuthenticator;

	public AuthController(
			AuthApplicationService authApplicationService,
			CharacterApplicationService characterApplicationService,
			SessionAuthenticator sessionAuthenticator) {
		this.authApplicationService = authApplicationService;
		this.characterApplicationService = characterApplicationService;
		this.sessionAuthenticator = sessionAuthenticator;
	}

	/**
	 * The account transaction commits before the session is created, so a rejected registration
	 * can never leave the caller authenticated as an account that was rolled back.
	 */
	@PostMapping("/auth/register")
	@ResponseStatus(HttpStatus.CREATED)
	public MeResponse register(
			@Valid @RequestBody RegisterRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		AccountView account = authApplicationService.register(request.email(), request.password());
		sessionAuthenticator.authenticate(account.email(), request.password(), httpRequest, httpResponse);
		return toMeResponse(account);
	}

	@PostMapping("/auth/login")
	public MeResponse login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		AccountPrincipal principal = sessionAuthenticator.authenticate(
				request.email(), request.password(), httpRequest, httpResponse);
		return toMeResponse(authApplicationService.currentUser(principal.getAccountId()));
	}

	@GetMapping("/me")
	public MeResponse me(@AuthenticationPrincipal AccountPrincipal principal) {
		return toMeResponse(authApplicationService.currentUser(principal.getAccountId()));
	}

	/**
	 * {@code hasCharacter} is composed here so the account application layer stays free of
	 * character-module dependencies.
	 */
	private MeResponse toMeResponse(AccountView account) {
		return new MeResponse(
				account.accountId(),
				account.email(),
				characterApplicationService.existsForAccount(account.accountId()));
	}
}
