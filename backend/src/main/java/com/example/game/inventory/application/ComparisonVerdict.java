package com.example.game.inventory.application;

import java.util.List;

public enum ComparisonVerdict {
	UPGRADE,
	DOWNGRADE,
	MIXED,
	SAME;

	public static ComparisonVerdict fromDeltas(List<StatDeltaView> deltas) {
		boolean improved = false;
		boolean worsened = false;
		if (deltas != null) {
			for (StatDeltaView delta : deltas) {
				if (delta.delta() > 0) {
					improved = true;
				}
				else if (delta.delta() < 0) {
					worsened = true;
				}
			}
		}
		if (improved && worsened) {
			return MIXED;
		}
		if (improved) {
			return UPGRADE;
		}
		if (worsened) {
			return DOWNGRADE;
		}
		return SAME;
	}
}
