package com.example.game.activity.api;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.activity.application.ActivityEntryView;

@RestController
@RequestMapping("/api/v1/activity")
public class ActivityController {

	private final ActivityApplicationService activityApplicationService;

	public ActivityController(ActivityApplicationService activityApplicationService) {
		this.activityApplicationService = activityApplicationService;
	}

	@GetMapping
	public List<ActivityEntryResponse> list(@AuthenticationPrincipal AccountPrincipal principal) {
		return activityApplicationService.listRecent(principal.getAccountId()).stream()
				.map(ActivityController::toResponse)
				.toList();
	}

	private static ActivityEntryResponse toResponse(ActivityEntryView view) {
		return new ActivityEntryResponse(
				view.id(),
				view.type(),
				view.message(),
				view.createdAt(),
				view.readAt());
	}
}
