package dev.scrollabletooltips.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lightweight JSON config. Uses Gson (bundled with Minecraft) and Fabric Loader's config
 * directory, so it pulls in NO extra dependencies (no Cloth Config, no Fabric API).
 *
 * <p>All fields are plain client-side rendering/input preferences. Nothing here touches the
 * network, the world, or the server in any way.
 */
public class ScrollableTooltipsConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "scrollabletooltips.json";

	/** Master toggle. When false the mod does nothing and vanilla tooltips render untouched. */
	public boolean enabled = true;

	/** How many GUI pixels to move per mouse-wheel notch. */
	public double scrollSpeed = 20.0;

	/** Invert the scroll direction. */
	public boolean invertScroll = false;

	/** Draw a small scrollbar on the right edge of a scrollable tooltip. */
	public boolean showScrollbar = true;

	/**
	 * Margin (in GUI pixels) kept between a scrollable (full-screen) tooltip and the screen edges.
	 * Lower = the tooltip fills more of the screen. Tooltips shorter than the screen are never
	 * scrolled and keep their normal near-cursor look.
	 */
	public int edgeMargin = 2;

	/**
	 * When true, logs (throttled) every intercepted tooltip's measured height and whether it will
	 * scroll. Handy for diagnosing "nothing happens" — if you never see these lines while hovering
	 * items, the mod isn't being invoked (usually a Minecraft version mismatch).
	 */
	public boolean debugLogging = false;

	// ----------------------------------------------------------------------------------------

	private static ScrollableTooltipsConfig instance;

	public static ScrollableTooltipsConfig get() {
		if (instance == null) {
			instance = load();
		}
		return instance;
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	public static ScrollableTooltipsConfig load() {
		Path path = configPath();
		ScrollableTooltipsConfig config;
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				config = GSON.fromJson(reader, ScrollableTooltipsConfig.class);
				if (config == null) {
					config = new ScrollableTooltipsConfig();
				}
			} catch (Exception e) {
				// Corrupt/incompatible config: fall back to defaults rather than crashing the game.
				config = new ScrollableTooltipsConfig();
			}
		} else {
			config = new ScrollableTooltipsConfig();
		}
		config.clamp();
		config.save();
		instance = config;
		return config;
	}

	public void save() {
		clamp();
		try {
			Path path = configPath();
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException ignored) {
			// Non-fatal: config simply won't persist this session.
		}
	}

	private void clamp() {
		if (scrollSpeed < 1.0) {
			scrollSpeed = 1.0;
		}
		if (edgeMargin < 0) {
			edgeMargin = 0;
		}
	}
}
