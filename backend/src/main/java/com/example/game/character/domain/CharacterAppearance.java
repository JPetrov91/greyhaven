package com.example.game.character.domain;

import java.util.Set;

/**
 * Cosmetic portrait catalog. Codes are the only values the server will persist.
 */
public final class CharacterAppearance {

	public static final String DEFAULT_MALE_AVATAR = "male_unyielding";
	public static final String DEFAULT_FEMALE_AVATAR = "female_veiled";

	private static final Set<String> MALE_AVATARS = Set.of(
			"male_unyielding",
			"male_iron_vow",
			"male_ashen_wolf",
			"male_pale_heir",
			"male_oathbound");

	private static final Set<String> FEMALE_AVATARS = Set.of(
			"female_veiled",
			"female_nightbloom",
			"female_silver_thorn",
			"female_ember_queen",
			"female_hollow_saint");

	private CharacterAppearance() {
	}

	public static CharacterGender resolveGender(CharacterGender gender) {
		return gender == null ? CharacterGender.MALE : gender;
	}

	public static String defaultAvatar(CharacterGender gender) {
		return resolveGender(gender) == CharacterGender.FEMALE
				? DEFAULT_FEMALE_AVATAR
				: DEFAULT_MALE_AVATAR;
	}

	public static boolean isAllowed(CharacterGender gender, String avatarCode) {
		if (gender == null || avatarCode == null) {
			return false;
		}
		return codesFor(gender).contains(avatarCode);
	}

	public static Set<String> codesFor(CharacterGender gender) {
		return gender == CharacterGender.FEMALE ? FEMALE_AVATARS : MALE_AVATARS;
	}

	public static Set<String> allCodes() {
		return Set.of(
				"male_unyielding",
				"male_iron_vow",
				"male_ashen_wolf",
				"male_pale_heir",
				"male_oathbound",
				"female_veiled",
				"female_nightbloom",
				"female_silver_thorn",
				"female_ember_queen",
				"female_hollow_saint");
	}
}
