# Scrollable Item Tooltips

A tiny **client-side** Fabric mod for **Minecraft 26.1.2** that lets you **scroll long item tooltips**
with the mouse wheel instead of letting them run off the top/bottom of the screen.

Great for:

- Long lore / book descriptions
- Huge enchantment lists
- Shulker box / bundle contents
- Modded items with wall-of-text descriptions

When a tooltip fits on screen it renders **exactly like vanilla** (normal size, near the cursor).
Only when a tooltip is **taller than the screen** does the mod step in: it pins the tooltip to the
top of the screen so it can **cover the full screen height**, and lets you **scroll the wheel** to
bring the off-screen portion into view.

---

## A note on the version number

Minecraft moved to a calendar-based versioning scheme. **`26.1.2` is the actual Minecraft
version** (it is the successor to `1.21.11`), not a mod version. `26.1` is also the **first
unobfuscated** Minecraft release, so it uses Mojang's official names and a new build toolchain.

This mod is built against:

| Thing              | Version                 |
| ------------------ | ----------------------- |
| Minecraft          | `26.1.2`                |
| Fabric Loader      | `>= 0.19.3`             |
| Fabric Loom        | `1.17` (unobfuscated)   |
| Gradle             | `9.5.1`                 |
| Java               | `25`                    |
| Fabric API         | **not required**        |

---

## Installation

1. Install **Fabric Loader** for Minecraft `26.1.2` using the [Fabric installer](https://fabricmc.net/use/).
2. Drop `scrollabletooltips-1.0.0.jar` into your `.minecraft/mods` folder.
3. Launch the game. That's it — **Fabric API is not needed**.

Then just hover an item with a long tooltip and **scroll the mouse wheel**.

---

## ✅ Client-side only / server-safe

This mod is designed to be completely invisible to servers:

- **`"environment": "client"`** in `fabric.mod.json`, and the only registered entrypoint is the
  **`client`** entrypoint (`dev.scrollabletooltips.client.ScrollableTooltipsClient`). There is no
  `main` entrypoint.
- All code lives in the **client source set** (`src/client/java`), so it is never shipped to or
  loaded on a dedicated server.
- The two Mixins only touch **client rendering/input** classes:
  - `net.minecraft.client.gui.GuiGraphicsExtractor` (how tooltips are drawn) — `@At("HEAD")`
  - `net.minecraft.client.MouseHandler` (the local mouse wheel) — `@At("HEAD")`
- **No packets** are sent or read. **No world/entity/block state** is touched. **No reach, timing,
  or interaction behaviour** is modified.
- Because it changes nothing the server can observe, it is **safe on servers that don't have the
  mod**, including large public servers with anti-cheat / mod detection. It does **not** need to be
  installed on the server.

---

## Compatibility

- The mod hooks the **vanilla tooltip render path** (`GuiGraphicsExtractor#tooltip`) rather than
  replacing it. Content that other mods funnel through the vanilla tooltip (e.g. JEI ingredient
  tooltips, WTHIT/Jade block tooltips) is scrolled too.
- When a tooltip **fits**, the mod bails out at `HEAD` and does nothing, so any other tooltip mod
  that injects later still runs normally.
- The mouse wheel is only intercepted **while an overflowing tooltip is actually on screen**. Every
  other scroll interaction (creative inventory list, JEI page scroll, hotbar selection, spectator
  fly speed, etc.) is left untouched.

---

## Configuration

A config file is created automatically at:

```
.minecraft/config/scrollabletooltips.json
```

It uses a lightweight JSON config powered by Gson (bundled with Minecraft) — **no Cloth Config or
other heavy dependencies**.

| Key             | Type    | Default | Description                                                                        |
| --------------- | ------- | ------- | ---------------------------------------------------------------------------------- |
| `enabled`       | boolean | `true`  | Master toggle. When `false`, vanilla tooltips render completely untouched.         |
| `scrollSpeed`   | double  | `20.0`  | GUI pixels moved per mouse-wheel notch.                                            |
| `invertScroll`  | boolean | `false` | Invert the scroll direction.                                                       |
| `showScrollbar` | boolean | `true`  | Draw a small scrollbar on the right edge of a scrollable tooltip.                  |
| `edgeMargin`    | int     | `8`     | Margin (GUI px) kept between a full-screen scrollable tooltip and the screen edges.|
| `debugLogging`  | boolean | `false` | Log (throttled) each intercepted tooltip's size + whether it will scroll.          |

Edit the file while the game is closed (it is read on startup).

> **Updating from an early version?** Older builds had a `maxTooltipHeight` option that made
> tooltips a fixed-height box. That option has been removed (tooltips now always use the full screen
> height). The stale value in your existing config is simply ignored — no action needed — but you
> can delete `scrollabletooltips.json` to regenerate a clean one.

---

## Troubleshooting ("it doesn't seem to be working")

Scrolling only kicks in when a tooltip is **taller than the screen**. Shorter tooltips render like
vanilla on purpose. If it seems inactive, work through this:

1. **Confirm the mod is actually loaded.** Look in your latest log
   (`.minecraft/logs/latest.log`) for:
   ```
   [Scrollable Item Tooltips] Client init complete (client-only, MC 26.1.2). ...
   ```
   If that line is missing, the mod didn't load. The #1 cause is a **Minecraft version
   mismatch** — this build only runs on **26.1.2**. `26.1` is unobfuscated, so a jar built for
   26.1.2 will *not* load on 1.21.x or other 26.1.x patches. Make sure your Fabric profile is
   `26.1.2` and that the server you're on (e.g. Hypixel) actually accepts a 26.1.2 client.

2. **Confirm tooltips are being intercepted.** Set `"debugLogging": true` in the config, restart,
   then hover items. You should see lines like:
   ```
   [Scrollable Item Tooltips] tooltip intercepted: lines=31, contentHeight=312px, windowHeight=272px, willScroll=true
   ```
   - `willScroll=false` just means that tooltip fit on your screen — try a taller one.
   - If you see **no** `tooltip intercepted` lines at all while hovering, the mod isn't being
     invoked → almost certainly the version mismatch from step 1.

3. **Then scroll the mouse wheel** while the (full-screen) tooltip is on screen. The wheel is only
   captured while an overflowing tooltip is visible; everywhere else it behaves normally.

This has been verified working on a real 26.1.2 client (a tooltip taller than the screen fills the
screen from the top and scrolls with the wheel).

---

## Building from source

Requires a **JDK 25**.

```bash
./gradlew build
```

The finished mod jar is written to `build/libs/scrollabletooltips-1.0.0.jar`
(ignore the `-sources.jar`).

---

## How it works (short version)

1. On every frame, `GuiGraphicsExtractor#extractDeferredElements` bumps a frame counter so the mod
   knows whether a scrollable tooltip is currently on screen.
2. `GuiGraphicsExtractor#tooltip` is **wrapped** (via MixinExtras `@WrapMethod`) rather than
   cancelled. The mod measures the tooltip; if it fits on screen it just calls the original
   (vanilla/other mods draw it normally). If it is taller than the screen, the mod:
   - enables a **scissor** clipped to the usable screen height,
   - translates the pose so the tooltip's top is pinned to the top margin, then panned by the
     current **scroll offset**,
   - calls the **original** renderer inside that transform (so vanilla — or another tooltip mod
     like Iconographic — still does the actual drawing), then draws an optional scrollbar.
3. `MouseHandler#onScroll` adjusts the scroll offset when the wheel moves and an overflowing
   tooltip is visible; otherwise it does nothing.

Wrapping (instead of cancelling) is what makes it play nicely with tooltip-styling mods: their
rendering runs untouched inside the original call — the mod only pans and clips around it.

## License

MIT — see [LICENSE](LICENSE).
