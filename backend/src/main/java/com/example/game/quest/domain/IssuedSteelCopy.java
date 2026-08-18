package com.example.game.quest.domain;

import java.time.Clock;

import com.example.game.item.domain.ItemCodes;

public final class IssuedSteelCopy {

	public static final String NODE_A =
			"Old Town has been eating drunks and runners. We are thin. I am not asking for a hero. I am asking for a pair of eyes that come back.\n"
					+ "The north road has gone quiet again — wagons overdue, same as last week. That is tomorrow's problem. Tonight I need the alleys walked.\n"
					+ "There is rust on the rack. Better rust in your hand than an empty one.";

	public static final String NODE_A2 =
			"Because you are standing here and I am short of living names. The watch does not wait for better volunteers.";

	public static final String NODE_B = "What can you hold?";

	public static final String AFTER_GRANT =
			"The Square is safe enough. The watch still pretends it is morning.\n"
					+ "Old Town is not. You will know it when the noise dies.\n"
					+ "Steel in your hand does not move your feet. Travel from the Square.\n"
					+ "When you are in the lanes — Search. That is how the street answers. Do not Search the Square. It has nothing to hide.\n"
					+ "Alive is a report.";

	public static final String PROGRESS_BEFORE_SEARCH =
			"The notice still stands. Old Town. Walk it. Then my desk.";

	public static final String CONFIRM_SWORD =
			"Then you stand, and you answer. Take the shield. It is as tired as the blade.";

	public static final String CONFIRM_AXE =
			"Then you finish it. The alley does not want a duel.";

	public static final String CONFIRM_MACE =
			"Then you knock sense through whatever they wear.";

	public static final String CONFIRM_DAGGERS =
			"Then you keep both hands busy. No shield. You will feel every mistake.";

	public static final String TURN_IN_VICTORY =
			"You came back louder than you left. That will do.\n"
					+ "Keep the steel. If the north road stays quiet, we will talk again.\n"
					+ "When the rust starts to embarrass you, Edric in the Market sells things that come back with you. That is not an order. Not yet.";

	public static final String TURN_IN_RETREAT =
			"Alive is a report. The alley will still be there tomorrow.\n"
					+ "Keep the steel anyway. I need you walking, not proud.\n"
					+ "Edric in the Market can put a better edge in your hand when you are ready. The rust stays honest until then.";

	public static final String TURN_IN_NO_FIGHT =
			"You walked it and came back. Most people only do the second part.\n"
					+ "Keep the steel. Old Town is not finished with anyone.\n"
					+ "The Market can wait. The alleys will not.";

	public static final String HINT_SHIELD = "Rusty weapon + shield";

	public static final String HINT_DAGGERS = "No shield";

	public static final String KIT_PREVIEW = "Rusty kit — chosen with Bren";

	public static final String SEARCH_FLAVOUR =
			"A shutter slams. Someone ran. The alley pretends it was the wind.";

	public static final String[] SEARCH_FLAVOUR_LINES = {
			SEARCH_FLAVOUR,
			"You find a dropped cap, still warm. No owner.",
			"Boots at the corner. Not in a hurry to hide."
	};

	private IssuedSteelCopy() {
	}

	public static String confirm(IssuedSteelKitFamily family) {
		return switch (family) {
			case SWORD -> CONFIRM_SWORD;
			case AXE -> CONFIRM_AXE;
			case MACE -> CONFIRM_MACE;
			case DAGGERS -> CONFIRM_DAGGERS;
		};
	}

	public static String weaponCode(IssuedSteelKitFamily family) {
		return switch (family) {
			case SWORD -> ItemCodes.RUSTY_SWORD;
			case AXE -> ItemCodes.RUSTY_AXE;
			case MACE -> ItemCodes.RUSTY_MACE;
			case DAGGERS -> ItemCodes.RUSTY_DAGGER;
		};
	}

	public static String searchFlavour(Clock clock) {
		int index = Math.floorMod((int) clock.instant().getEpochSecond(), SEARCH_FLAVOUR_LINES.length);
		return SEARCH_FLAVOUR_LINES[index];
	}

	public static String turnIn(IssuedSteelSearchOutcome outcome) {
		if (outcome == IssuedSteelSearchOutcome.VICTORY) {
			return TURN_IN_VICTORY;
		}
		if (outcome == IssuedSteelSearchOutcome.RETREAT) {
			return TURN_IN_RETREAT;
		}
		return TURN_IN_NO_FIGHT;
	}
}
