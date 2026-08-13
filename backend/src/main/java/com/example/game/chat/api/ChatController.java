package com.example.game.chat.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.chat.application.ChatApplicationService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

	private final ChatApplicationService chatApplicationService;
	private final ChatSseHub chatSseHub;

	public ChatController(ChatApplicationService chatApplicationService, ChatSseHub chatSseHub) {
		this.chatApplicationService = chatApplicationService;
		this.chatSseHub = chatSseHub;
	}

	@GetMapping("/messages")
	public List<ChatMessageResponse> list(@AuthenticationPrincipal AccountPrincipal principal) {
		return chatApplicationService.listRecent(principal.getAccountId()).stream()
				.map(ChatMessageResponse::from)
				.toList();
	}

	@PostMapping("/messages")
	public ChatMessageResponse post(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody CreateChatMessageRequest request) {
		return ChatMessageResponse.from(chatApplicationService.post(principal.getAccountId(), request.body()));
	}

	@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter stream(
			@AuthenticationPrincipal AccountPrincipal principal,
			@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
			@RequestParam(value = "after", required = false) UUID after,
			HttpServletResponse response) {
		chatApplicationService.assertCanListen(principal.getAccountId());
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("X-Accel-Buffering", "no");
		UUID afterId = after != null ? after : parseUuid(lastEventId);
		List<ChatMessageResponse> missed = afterId == null
				? List.of()
				: chatApplicationService.listAfter(principal.getAccountId(), afterId).stream()
						.map(ChatMessageResponse::from)
						.toList();
		return chatSseHub.open(missed);
	}

	private static UUID parseUuid(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return UUID.fromString(value.trim());
		}
		catch (IllegalArgumentException exception) {
			return null;
		}
	}
}
