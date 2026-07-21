package dev.scrollabletooltips.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.scrollabletooltips.client.ScrollableTooltipsClient;
import dev.scrollabletooltips.client.ScrollableTooltipsConfig;
import dev.scrollabletooltips.client.TooltipScrollState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Client-only Mixin into vanilla's tooltip rendering.
 *
 * <p>In Minecraft 26.1, GUI drawing was refactored around {@code GuiGraphicsExtractor}. All item
 * tooltips (lore, enchantments, shulker contents, modded descriptions, and content that other mods
 * funnel through the vanilla path) are ultimately rendered by {@link GuiGraphicsExtractor#tooltip}.
 *
 * <p>Rather than cancelling and re-implementing that method (which fights with tooltip-styling mods
 * such as Iconographic that also take over tooltip rendering), we <b>wrap</b> it with MixinExtras'
 * {@code @WrapMethod}. The original rendering — vanilla's or another mod's — still runs inside
 * {@code original.call(...)}; we simply:
 *
 * <ul>
 *   <li>measure the tooltip and, if it is taller than the usable screen height, pin its top to the
 *       top margin, clip everything to a full-screen window with a scissor, and translate the whole
 *       tooltip by the current scroll offset so the off-screen part can be scrolled into view;</li>
 *   <li>otherwise call the original untouched, so short tooltips look exactly like normal.</li>
 * </ul>
 *
 * <p>This is purely local rendering. No packets, no world/entity access, nothing the server sees.
 */
@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin {

	/** Vanilla background padding around the tooltip content (top + bottom), in GUI pixels. */
	@Unique
	private static final int BACKGROUND_PADDING = 3;

	/**
	 * Extra breathing room (in GUI pixels) kept between the tooltip's colored border and the clip
	 * window's top/bottom edge. Without this the 1px border sits exactly on the scissor boundary at
	 * the scroll extremes and gets shaved off. The scroll range is extended by the same amount so
	 * the whole tooltip is still reachable.
	 */
	@Unique
	private static final int BORDER_INSET = 2;

	@WrapMethod(method = "tooltip")
	private void scrollabletooltips$wrapTooltip(Font font, List<ClientTooltipComponent> lines, int xo, int yo,
			ClientTooltipPositioner positioner, Identifier style, Operation<Void> original) {
		ScrollableTooltipsConfig config = ScrollableTooltipsConfig.get();
		if (!config.enabled || lines.isEmpty()) {
			original.call(font, lines, xo, yo, positioner, style);
			return;
		}

		GuiGraphicsExtractor self = (GuiGraphicsExtractor) (Object) this;

		// Measure exactly like vanilla: h is the value vanilla passes to the positioner.
		int width = 0;
		int contentHeight = lines.size() == 1 ? -2 : 0;
		int signature = lines.size();
		for (int i = 0; i < lines.size(); i++) {
			ClientTooltipComponent line = lines.get(i);
			int lineWidth = line.getWidth(font);
			int lineHeight = line.getHeight(font);
			if (lineWidth > width) {
				width = lineWidth;
			}
			contentHeight += lineHeight;
			signature = signature * 31 + lineWidth;
			signature = signature * 31 + lineHeight;
			signature = signature * 31 + line.getClass().hashCode();
		}

		int topMargin = config.edgeMargin;
		// The scroll window is always the full usable screen height.
		int windowHeight = self.guiHeight() - config.edgeMargin * 2;

		if (config.debugLogging) {
			ScrollableTooltipsClient.debugTooltip(lines.size(), contentHeight, windowHeight);
		}

		// Fits on screen -> render normally near the cursor (vanilla / other mods untouched).
		if (windowHeight <= 0 || contentHeight <= windowHeight) {
			TooltipScrollState.onFits(signature);
			original.call(font, lines, xo, yo, positioner, style);
			return;
		}

		// Predict exactly where the original will position the box (same positioner + args), so we
		// can pin its top to the top margin regardless of the positioner's own off-screen clamping.
		Vector2ic predicted = positioner.positionTooltip(self.guiWidth(), self.guiHeight(), xo, yo, width, contentHeight);
		int predictedTop = predicted.y();

		// Full rendered extent including background padding above and below.
		int fullExtent = contentHeight + BACKGROUND_PADDING * 2;
		// Pin/scroll the content inside a slightly inset region so the colored border never lands on
		// the scissor edge (which would clip a 1px line at the very top/bottom).
		int contentTop = topMargin + BORDER_INSET;
		int contentWindow = Math.max(1, windowHeight - BORDER_INSET * 2);
		double maxScroll = Math.max(0, fullExtent - contentWindow);
		double offset = TooltipScrollState.onOverflowRender(signature, maxScroll);

		// Shift so the (padded) top sits at the inset top, then pan down by the scroll offset.
		float translateY = (contentTop - (predictedTop - BACKGROUND_PADDING)) - (float) offset;

		self.pose().pushMatrix();
		// Clip vertically to the usable window. enableScissor bakes in the current pose, so it must
		// run BEFORE we translate.
		self.enableScissor(0, topMargin, self.guiWidth(), topMargin + windowHeight);
		self.pose().translate(0.0F, translateY);

		// Let the original renderer (vanilla or e.g. Iconographic) draw the full tooltip; our pose
		// translation + scissor turn it into a scrollable window.
		original.call(font, lines, xo, yo, positioner, style);

		self.pose().popMatrix();
		self.disableScissor();

		if (config.showScrollbar) {
			scrollabletooltips$renderScrollbar(self, predicted.x() + width, topMargin, windowHeight, offset, maxScroll, fullExtent);
		}
	}

	@Unique
	private static void scrollabletooltips$renderScrollbar(GuiGraphicsExtractor self, int right, int top,
			int windowHeight, double offset, double maxScroll, int fullExtent) {
		int barLeft = right + 1;
		int barRight = right + 3;

		// Faint track.
		self.fill(barLeft, top, barRight, top + windowHeight, 0x40FFFFFF);

		// Thumb sized proportionally to how much of the content is visible.
		int thumbHeight = Math.max(8, (int) ((long) windowHeight * windowHeight / Math.max(1, fullExtent)));
		double fraction = maxScroll <= 0 ? 0.0 : offset / maxScroll;
		int thumbTop = top + (int) Math.round((windowHeight - thumbHeight) * fraction);
		self.fill(barLeft, thumbTop, barRight, thumbTop + thumbHeight, 0xFFFFFFFF);
	}

	@Inject(method = "extractDeferredElements", at = @At("HEAD"))
	private void scrollabletooltips$beginFrame(int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		TooltipScrollState.beginFrame();
	}
}
