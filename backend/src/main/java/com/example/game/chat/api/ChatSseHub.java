package com.example.game.chat.api;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.game.chat.application.ChatMessagePublisher;
import com.example.game.chat.application.ChatMessageView;

import jakarta.annotation.PreDestroy;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class ChatSseHub implements ChatMessagePublisher {

	private static final Logger log = LoggerFactory.getLogger(ChatSseHub.class);
	private static final long EMITTER_TIMEOUT_MS = 300_000L;

	private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
	private final ObjectMapper objectMapper;
	private final ScheduledExecutorService keepAlive = Executors.newSingleThreadScheduledExecutor(thread -> {
		Thread worker = new Thread(thread, "chat-sse-keepalive");
		worker.setDaemon(true);
		return worker;
	});

	public ChatSseHub(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		keepAlive.scheduleAtFixedRate(this::sendKeepAlives, 15, 15, TimeUnit.SECONDS);
	}

	public SseEmitter open(List<ChatMessageResponse> missed) {
		SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
		emitters.add(emitter);
		emitter.onCompletion(() -> emitters.remove(emitter));
		emitter.onTimeout(() -> {
			emitters.remove(emitter);
			emitter.complete();
		});
		emitter.onError(error -> emitters.remove(emitter));
		for (ChatMessageResponse message : missed) {
			if (!send(emitter, message)) {
				break;
			}
		}
		return emitter;
	}

	@Override
	public void publish(ChatMessageView message) {
		ChatMessageResponse payload = ChatMessageResponse.from(message);
		for (SseEmitter emitter : emitters) {
			send(emitter, payload);
		}
	}

	@PreDestroy
	public void shutdown() {
		keepAlive.shutdownNow();
		for (SseEmitter emitter : emitters) {
			emitter.complete();
		}
		emitters.clear();
	}

	private void sendKeepAlives() {
		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event().comment("keepalive"));
			}
			catch (IOException | IllegalStateException exception) {
				emitters.remove(emitter);
				emitter.complete();
			}
		}
	}

	private boolean send(SseEmitter emitter, ChatMessageResponse message) {
		try {
			emitter.send(SseEmitter.event()
					.id(message.id().toString())
					.name("message")
					.data(objectMapper.writeValueAsString(message)));
			return true;
		}
		catch (IOException | IllegalStateException | JacksonException exception) {
			log.debug("Dropping chat SSE subscriber after send failure", exception);
			emitters.remove(emitter);
			emitter.complete();
			return false;
		}
	}
}
