# Scrollable Item Tooltips

A tiny **client-side** Fabric mod for **Minecraft 26.1.2** that lets you **scroll long item tooltips**
with the mouse wheel instead of letting them run off the screen — great for long lore, huge
enchantment lists, shulker/bundle contents, and modded wall-of-text descriptions.

Tooltips that fit render exactly like vanilla. Only when a tooltip is **taller than the screen** does
the mod step in: it fills the screen height and lets you **scroll the wheel** to reveal the rest.

## Installation

1. Install **Fabric Loader** for Minecraft `26.1.2`.
2. Drop `scrollabletooltips-1.0.0.jar` into `.minecraft/mods`.
3. Launch — **Fabric API is not required**.

## Client-side & server-safe

100% client-side (`"environment": "client"`, client entrypoint only, code in `src/client/java`). The
Mixins only touch client rendering/input (`GuiGraphicsExtractor`, `MouseHandler`) — no packets, no
world/entity access, no reach/timing changes. Safe on servers that don't have it, including public
servers with anti-cheat. Does not need to be installed server-side.

## Compatibility

Hooks the vanilla tooltip path (`GuiGraphicsExtractor#tooltip`) instead of replacing it, so tooltips
from JEI, WTHIT/Jade, etc. scroll too. When a tooltip fits, the mod does nothing. The wheel is only
intercepted while an overflowing tooltip is on screen; all other scrolling is untouched.

## Configuration

Auto-created at `.minecraft/config/scrollabletooltips.json` (plain JSON via Gson, no extra deps).
Edit while the game is closed.

| Key             | Default | Description                                                     |
| --------------- | ------- | --------------------------------------------------------------- |
| `enabled`       | `true`  | Master toggle.                                                  |
| `scrollSpeed`   | `20.0`  | GUI pixels moved per wheel notch.                               |
| `invertScroll`  | `false` | Invert scroll direction.                                        |
| `showScrollbar` | `true`  | Draw a scrollbar on the right edge.                             |
| `edgeMargin`    | `8`     | Margin (px) kept between the tooltip and the screen edges.      |
| `debugLogging`  | `false` | Log each intercepted tooltip's size (for troubleshooting).      |

## Building

Requires **JDK 25**. Run `./gradlew build`; the jar is written to
`build/libs/scrollabletooltips-1.0.0.jar`.

## License

MIT — see [LICENSE](LICENSE).
