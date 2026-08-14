package com.example.game.item.domain;

public enum ArmorCategory {
	LIGHT,
	MEDIUM,
	HEAVY;

	public int weightRank() {
		return switch (this) {
			case LIGHT -> 1;
			case MEDIUM -> 2;
			case HEAVY -> 3;
		};
	}

	public static ArmorCategory heaviest(ArmorCategory left, ArmorCategory right) {
		if (left == null) {
			return right;
		}
		if (right == null) {
			return left;
		}
		return left.weightRank() >= right.weightRank() ? left : right;
	}
}
