package com.example.game.character.api;

import java.util.List;

public record CharacterRosterResponse(List<CharacterSlotResponse> slots) {
}
