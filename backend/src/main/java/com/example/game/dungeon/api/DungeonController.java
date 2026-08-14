package com.example.game.dungeon.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.dungeon.application.DungeonApplicationService;
import com.example.game.dungeon.application.DungeonRunView;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/dungeons")
public class DungeonController {

	private final DungeonApplicationService dungeonApplicationService;

	public DungeonController(DungeonApplicationService dungeonApplicationService) {
		this.dungeonApplicationService = dungeonApplicationService;
	}

	@GetMapping("/current")
	public ResponseEntity<DungeonRunResponse> current(@AuthenticationPrincipal AccountPrincipal principal) {
		DungeonRunView view = dungeonApplicationService.current(principal.getAccountId());
		if (view == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(DungeonRunResponse.from(view));
	}

	@PostMapping("/enter")
	public DungeonRunResponse enter(@AuthenticationPrincipal AccountPrincipal principal) {
		return DungeonRunResponse.from(dungeonApplicationService.enter(principal.getAccountId()));
	}

	@PostMapping("/leave")
	public DungeonRunResponse leave(@AuthenticationPrincipal AccountPrincipal principal) {
		return DungeonRunResponse.from(dungeonApplicationService.leave(principal.getAccountId()));
	}

	@PostMapping("/abandon")
	public DungeonRunResponse abandon(@AuthenticationPrincipal AccountPrincipal principal) {
		return DungeonRunResponse.from(dungeonApplicationService.abandon(principal.getAccountId()));
	}

	@PostMapping("/advance")
	public DungeonRunResponse advance(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody DungeonAdvanceRequest request) {
		return DungeonRunResponse.from(dungeonApplicationService.advance(principal.getAccountId(), request.edgeCode()));
	}
}
