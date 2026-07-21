package com.terraformersmc.modmenu.api;

/**
 * Minimal compile-time stub of Mod Menu's API marker interface.
 *
 * <p>This lets us implement Mod Menu integration <b>without</b> a hard build- or run-time dependency
 * on Mod Menu. These stub classes are excluded from the built jar (see {@code build.gradle}); at
 * runtime the real interfaces are provided by the user's Mod Menu install. If Mod Menu isn't
 * installed, our {@code modmenu} entrypoint is simply never queried, so this class is never loaded.
 */
public interface ModMenuApiMarker {
}
