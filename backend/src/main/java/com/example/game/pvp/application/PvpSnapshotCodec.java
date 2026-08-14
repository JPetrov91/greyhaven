package com.example.game.pvp.application;

import org.springframework.stereotype.Component;

import com.example.game.pvp.domain.PvpMatchSnapshot;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class PvpSnapshotCodec {

	private final ObjectMapper objectMapper;

	public PvpSnapshotCodec(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String write(PvpMatchSnapshot snapshot) {
		try {
			return objectMapper.writeValueAsString(snapshot);
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Failed to serialize PvP snapshot", exception);
		}
	}

	public PvpMatchSnapshot read(String payload) {
		try {
			return objectMapper.readValue(payload, PvpMatchSnapshot.class);
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Failed to read PvP snapshot", exception);
		}
	}
}
