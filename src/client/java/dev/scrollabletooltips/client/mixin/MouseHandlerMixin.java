package dev.scrollabletooltips.client.mixin;

import dev.scrollabletooltips.client.TooltipScrollState;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client-only Mixin that intercepts the mouse wheel at its single dispatch point,
 * {@link MouseHandler#onScroll}. This covers every context (vanilla screens, modded screens, JEI
 * overlays, ...).
 *
 * <p>We only consume the event when an overflowing tooltip is currently on screen; otherwise the
 * wheel behaves exactly as vanilla (moving the hotbar selection, scrolling inventory lists, etc.).
 *
 * <p>This handler reads local UI state and, at most, cancels a local input event. It sends no
 * packets and does not touch timing/reach/world interaction, so there is nothing here for a
 * server or anti-cheat to detect.
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void scrollabletooltips$onScroll(long window, double horizontalAmount, double verticalAmount, CallbackInfo ci) {
		if (TooltipScrollState.handleScroll(verticalAmount)) {
			ci.cancel();
		}
	}
}
