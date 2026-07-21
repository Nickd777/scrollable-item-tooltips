package dev.scrollabletooltips.client.gui;

import dev.scrollabletooltips.client.ScrollableTooltipsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;

/**
 * In-game settings screen for the mod, opened via Mod Menu's "Configure" button.
 *
 * <p>Built entirely from vanilla widgets (toggles + sliders), so it needs no Cloth Config or other
 * config library. Changes are written straight to the live {@link ScrollableTooltipsConfig} and
 * saved to disk when the screen closes, so they take effect immediately.
 */
public class ScrollableTooltipsConfigScreen extends Screen {

	private static final int ROW_WIDTH = 220;
	private static final int ROW_HEIGHT = 20;
	private static final int ROW_SPACING = 24;

	private final Screen parent;

	public ScrollableTooltipsConfigScreen(Screen parent) {
		super(Minecraft.getInstance(), Minecraft.getInstance().font, Component.literal("Scrollable Item Tooltips"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ScrollableTooltipsConfig config = ScrollableTooltipsConfig.get();

		StringWidget titleWidget = new StringWidget(this.title, this.font);
		titleWidget.setX((this.width - titleWidget.getWidth()) / 2);
		titleWidget.setY(15);
		addRenderableWidget(titleWidget);

		int x = (this.width - ROW_WIDTH) / 2;
		int y = 40;

		addRenderableWidget(CycleButton.onOffBuilder(config.enabled)
				.create(x, y, ROW_WIDTH, ROW_HEIGHT, Component.literal("Enabled"),
						(button, value) -> config.enabled = value));
		y += ROW_SPACING;

		addRenderableWidget(new OptionSlider(x, y, ROW_WIDTH, ROW_HEIGHT, "Scroll Speed", 1, 100,
				config.scrollSpeed, value -> config.scrollSpeed = value));
		y += ROW_SPACING;

		addRenderableWidget(CycleButton.onOffBuilder(config.invertScroll)
				.create(x, y, ROW_WIDTH, ROW_HEIGHT, Component.literal("Invert Scroll"),
						(button, value) -> config.invertScroll = value));
		y += ROW_SPACING;

		addRenderableWidget(CycleButton.onOffBuilder(config.showScrollbar)
				.create(x, y, ROW_WIDTH, ROW_HEIGHT, Component.literal("Show Scrollbar"),
						(button, value) -> config.showScrollbar = value));
		y += ROW_SPACING;

		addRenderableWidget(new OptionSlider(x, y, ROW_WIDTH, ROW_HEIGHT, "Edge Margin", 0, 32,
				config.edgeMargin, value -> config.edgeMargin = (int) Math.round(value)));
		y += ROW_SPACING;

		addRenderableWidget(CycleButton.onOffBuilder(config.debugLogging)
				.create(x, y, ROW_WIDTH, ROW_HEIGHT, Component.literal("Debug Logging"),
						(button, value) -> config.debugLogging = value));

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
				.bounds((this.width - 200) / 2, this.height - 28, 200, ROW_HEIGHT)
				.build());
	}

	@Override
	public void onClose() {
		// Persist and re-clamp on the way out; changes are already live in the singleton.
		ScrollableTooltipsConfig.get().save();
		this.minecraft.setScreen(parent);
	}

	/** A simple integer-valued slider mapping the widget's 0..1 position onto {@code [min, max]}. */
	private static class OptionSlider extends AbstractSliderButton {

		private final String label;
		private final double min;
		private final double max;
		private final DoubleConsumer setter;

		OptionSlider(int x, int y, int width, int height, String label, double min, double max,
				double current, DoubleConsumer setter) {
			super(x, y, width, height, Component.empty(), (current - min) / (max - min));
			this.label = label;
			this.min = min;
			this.max = max;
			this.setter = setter;
			updateMessage();
		}

		private double actualValue() {
			return min + this.value * (max - min);
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.literal(label + ": " + (int) Math.round(actualValue())));
		}

		@Override
		protected void applyValue() {
			setter.accept((double) Math.round(actualValue()));
		}
	}
}
