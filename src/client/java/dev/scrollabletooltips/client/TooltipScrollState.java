package dev.scrollabletooltips.client;

import net.minecraft.client.Minecraft;

/**
 * Holds the transient scroll state for the tooltip currently under the mouse.
 *
 * <p>Everything in here is pure client-side UI state: a scroll offset in GUI pixels, a cheap
 * signature used to detect when the hovered content changes (so we can reset the offset), and a
 * frame counter used to know whether an overflowing tooltip was actually rendered "just now".
 *
 * <p>This class is only ever touched from the render thread, so it needs no synchronization.
 */
public final class TooltipScrollState {

	private TooltipScrollState() {
	}

	/** Current scroll offset, in GUI pixels (0 == top of the content). */
	private static double offset;

	/** Maximum scroll offset for the tooltip rendered this frame. */
	private static double maxScroll;

	/** Signature of the content currently being shown, used to detect content changes. */
	private static int contentSignature;

	/** Whether the tooltip rendered on the most recent frame was tall enough to need scrolling. */
	private static boolean overflowActive;

	/** Increments once per rendered frame. Used to detect stale state. */
	private static long frameCounter;

	/** The frame index on which an overflowing tooltip was last rendered. */
	private static long lastOverflowFrame = -10;

	/** Called at the start of every GUI frame (before deferred tooltips run). */
	public static void beginFrame() {
		frameCounter++;
		// If no overflowing tooltip has been rendered for a couple of frames, treat scrolling as
		// inactive so we stop intercepting the mouse wheel.
		if (overflowActive && frameCounter - lastOverflowFrame > 1) {
			overflowActive = false;
		}
	}

	/**
	 * Called by the tooltip render mixin when it takes over rendering of an overflowing tooltip.
	 *
	 * @return the clamped scroll offset to apply this frame
	 */
	public static double onOverflowRender(int signature, double newMaxScroll) {
		if (signature != contentSignature) {
			// A different tooltip is now on screen (e.g. the mouse moved to another item): start
			// from the top.
			contentSignature = signature;
			offset = 0.0;
		}
		maxScroll = Math.max(0.0, newMaxScroll);
		offset = clamp(offset);
		overflowActive = true;
		lastOverflowFrame = frameCounter;
		return offset;
	}

	/** Called by the tooltip render mixin when the hovered tooltip fits without scrolling. */
	public static void onFits(int signature) {
		if (signature != contentSignature) {
			contentSignature = signature;
			offset = 0.0;
		}
		overflowActive = false;
	}

	/**
	 * Handle a mouse-wheel event. Returns true if the event was consumed to scroll a tooltip
	 * (in which case the caller should cancel the vanilla scroll handling).
	 */
	public static boolean handleScroll(double verticalAmount) {
		ScrollableTooltipsConfig config = ScrollableTooltipsConfig.get();
		if (!config.enabled) {
			return false;
		}
		// Only intercept the wheel while an overflowing tooltip is actually on screen. This keeps
		// us out of the way of every other scroll interaction (creative inventory list, JEI, etc.).
		if (!overflowActive || frameCounter - lastOverflowFrame > 1) {
			return false;
		}
		// Belt-and-braces: a scrollable tooltip only ever appears while a screen is open.
		if (Minecraft.getInstance().screen == null) {
			return false;
		}
		double direction = config.invertScroll ? 1.0 : -1.0;
		offset = clamp(offset + verticalAmount * config.scrollSpeed * direction);
		return true;
	}

	private static double clamp(double value) {
		if (value < 0.0) {
			return 0.0;
		}
		if (value > maxScroll) {
			return maxScroll;
		}
		return value;
	}
}
