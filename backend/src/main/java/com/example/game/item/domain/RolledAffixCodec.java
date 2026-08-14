package com.example.game.item.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact persistence for planned affix rolls on combat/expedition reward rows.
 */
public final class RolledAffixCodec {

	private RolledAffixCodec() {
	}

	public static String encode(List<RolledAffix> affixes) {
		if (affixes == null || affixes.isEmpty()) {
			return "";
		}
		StringBuilder encoded = new StringBuilder();
		for (RolledAffix affix : affixes) {
			if (!encoded.isEmpty()) {
				encoded.append('|');
			}
			encoded.append(affix.kind().name())
					.append(':')
					.append(affix.affixCode())
					.append(':')
					.append(affix.ordinal())
					.append(':')
					.append(affix.magnitude());
		}
		return encoded.toString();
	}

	public static List<RolledAffix> decode(String raw) {
		if (raw == null || raw.isBlank()) {
			return List.of();
		}
		List<RolledAffix> affixes = new ArrayList<>();
		for (String token : raw.split("\\|")) {
			String[] parts = token.split(":", 4);
			if (parts.length != 4) {
				throw new IllegalArgumentException("invalid rolled affix encoding: " + raw);
			}
			affixes.add(new RolledAffix(
					parts[1],
					AffixKind.valueOf(parts[0]),
					Integer.parseInt(parts[2]),
					Integer.parseInt(parts[3])));
		}
		return List.copyOf(affixes);
	}
}
