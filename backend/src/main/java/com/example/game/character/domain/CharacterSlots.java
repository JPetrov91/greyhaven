package com.example.game.character.domain;

public final class CharacterSlots {

	public static final int MAX = 3;

	private CharacterSlots() {
	}

	public static boolean validIndex(int slotIndex) {
		return slotIndex >= 0 && slotIndex < MAX;
	}
}
