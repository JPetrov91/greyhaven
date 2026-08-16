package com.example.game.world.application;

public record NpcTalkActionView(
		String type,
		String questCode,
		String merchantCode,
		String label,
		String hint,
		String action
) {

	public NpcTalkActionView(String type, String questCode, String merchantCode, String label) {
		this(type, questCode, merchantCode, label, null, null);
	}
}
