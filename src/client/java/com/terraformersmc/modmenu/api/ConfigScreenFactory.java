package com.terraformersmc.modmenu.api;

import net.minecraft.client.gui.screens.Screen;

/**
 * Compile-time stub of Mod Menu's {@code ConfigScreenFactory} (see {@link ModMenuApiMarker}).
 * Excluded from the built jar; the real interface comes from Mod Menu at runtime.
 */
@FunctionalInterface
public interface ConfigScreenFactory<S extends Screen> {
	S create(Screen parent);
}
