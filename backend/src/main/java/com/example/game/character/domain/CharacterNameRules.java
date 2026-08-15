package com.example.game.character.domain;

/**
 * Display names may use letters, digits, and single spaces. Special characters are rejected.
 */
public final class CharacterNameRules {

	public static final String PATTERN = "^[\\p{L}\\p{N}]+(?: [\\p{L}\\p{N}]+)*$";
	public static final String MESSAGE = "letters, digits, and spaces only; no special characters";

	private CharacterNameRules() {
	}
}
