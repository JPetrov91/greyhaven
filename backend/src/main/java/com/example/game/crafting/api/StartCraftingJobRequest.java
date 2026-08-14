package com.example.game.crafting.api;

import jakarta.validation.constraints.NotBlank;

public record StartCraftingJobRequest(@NotBlank String recipeCode) {
}
