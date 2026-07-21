package com.terraformersmc.modmenu.api;

/**
 * Compile-time stub of Mod Menu's {@code ModMenuApi} (see {@link ModMenuApiMarker}). Only the one
 * method we override is declared. Excluded from the built jar; the real interface (with all its
 * other default methods) is provided by Mod Menu at runtime.
 */
public interface ModMenuApi extends ModMenuApiMarker {
	default ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> null;
	}
}
