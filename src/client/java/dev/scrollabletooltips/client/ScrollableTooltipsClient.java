package dev.scrollabletooltips.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint. This mod is registered ONLY under the "client" entrypoint in
 * fabric.mod.json and lives entirely in the client source set, so none of its code can ever be
 * loaded on a dedicated server.
 *
 * <p>All this does is eagerly load the config file. The actual behaviour is implemented via
 * two client-only Mixins into vanilla client rendering/input classes
 * ({@code GuiGraphicsExtractor} and {@code MouseHandler}). There are no packets, no world/entity
 * access, and no server-side hooks whatsoever.
 */
public class ScrollableTooltipsClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("scrollabletooltips");

	private static long lastDebugTooltipLog = 0;

	@Override
	public void onInitializeClient() {
		// Load (and create if missing) the config up-front.
		ScrollableTooltipsConfig.load();
		LOGGER.info("[Scrollable Item Tooltips] Client init complete (client-only, MC 26.1.2). scrollSpeed={}, edgeMargin={}, debugLogging={}",
				ScrollableTooltipsConfig.get().scrollSpeed, ScrollableTooltipsConfig.get().edgeMargin,
				ScrollableTooltipsConfig.get().debugLogging);
	}

	/** Throttled (once/second) debug log for intercepted tooltips. Enabled via {@code debugLogging}. */
	public static void debugTooltip(int lines, int contentHeight, int windowHeight) {
		long now = System.currentTimeMillis();
		if (now - lastDebugTooltipLog < 1000) {
			return;
		}
		lastDebugTooltipLog = now;
		LOGGER.info("[Scrollable Item Tooltips] tooltip intercepted: lines={}, contentHeight={}px, windowHeight={}px, willScroll={}",
				lines, contentHeight, windowHeight, contentHeight > windowHeight);
	}
}
