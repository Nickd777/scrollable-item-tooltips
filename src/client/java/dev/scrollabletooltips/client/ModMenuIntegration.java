package dev.scrollabletooltips.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.scrollabletooltips.client.gui.ScrollableTooltipsConfigScreen;

/**
 * Mod Menu entrypoint. Registered under the (optional) {@code modmenu} entrypoint in
 * fabric.mod.json, so it is only ever loaded when the user actually has Mod Menu installed. When
 * they don't, nothing here is touched and the mod runs exactly the same.
 *
 * <p>This gives the mod a "Configure" button on its Mod Menu entry that opens
 * {@link ScrollableTooltipsConfigScreen}.
 */
public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ScrollableTooltipsConfigScreen::new;
	}
}
