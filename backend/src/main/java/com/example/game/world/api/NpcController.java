package com.example.game.world.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.world.application.NpcApplicationService;
import com.example.game.world.application.NpcTalkActionView;
import com.example.game.world.application.NpcTalkView;
import com.example.game.world.application.NpcView;

@RestController
@RequestMapping("/api/v1/world/npcs")
public class NpcController {

	private final NpcApplicationService npcApplicationService;

	public NpcController(NpcApplicationService npcApplicationService) {
		this.npcApplicationService = npcApplicationService;
	}

	@GetMapping
	public NpcListResponse list(@AuthenticationPrincipal AccountPrincipal principal) {
		return new NpcListResponse(
				npcApplicationService.atCurrentLocation(principal.getAccountId()).stream()
						.map(NpcController::toResponse)
						.toList());
	}

	@GetMapping("/{code}")
	public NpcResponse get(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("code") String code) {
		return toResponse(npcApplicationService.get(principal.getAccountId(), code));
	}

	@PostMapping("/{code}/talk")
	public NpcTalkResponse talk(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("code") String code,
			@RequestBody(required = false) NpcTalkRequest request) {
		String questCode = request == null ? null : request.questCode();
		String action = request == null ? null : request.action();
		String kitFamily = request == null ? null : request.kitFamily();
		return toTalkResponse(npcApplicationService.talk(principal.getAccountId(), code, questCode, action, kitFamily));
	}

	private static NpcResponse toResponse(NpcView view) {
		return new NpcResponse(
				view.code(),
				view.name(),
				view.title(),
				view.description(),
				view.greeting(),
				view.portraitCode(),
				view.locationCode(),
				view.merchantCode(),
				view.interactions(),
				view.questBadges());
	}

	private static NpcTalkResponse toTalkResponse(NpcTalkView view) {
		return new NpcTalkResponse(
				view.code(),
				view.name(),
				view.title(),
				view.portraitCode(),
				view.text(),
				view.merchantCode(),
				view.actions().stream().map(NpcController::toAction).toList());
	}

	private static NpcTalkActionResponse toAction(NpcTalkActionView view) {
		return new NpcTalkActionResponse(
				view.type(),
				view.questCode(),
				view.merchantCode(),
				view.label(),
				view.hint(),
				view.action());
	}
}
