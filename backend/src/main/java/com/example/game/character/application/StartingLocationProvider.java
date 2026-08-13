package com.example.game.character.application;

import java.util.UUID;

/**
 * Supplies the location a newly created character is placed in. Implemented by the world module,
 * which owns the location graph, so character creation does not depend on world persistence.
 */
public interface StartingLocationProvider {

	UUID startingLocationId();
}
