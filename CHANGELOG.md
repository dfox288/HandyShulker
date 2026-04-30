# Changelog

## 2.1.0-beta.3

### Breaking
- **Mod ID renamed from `handyshulkers` to `handyshulker`** to match the rest of the Handy series convention (singular). Existing config at `config/handyshulkers.json` is migrated automatically on first launch — no settings lost.
- Internal package moved from `dev.handyshulkers` to `dev.handy.mods.handyshulker`. No user-facing impact unless another mod was depending on internal classes.

### Bug fixes
- **Fixed rare `ClassCastException` on shulker scroll-extract** when another mod constructed an `AbstractContainerMenu` subclass that loaded before our mixin applied. The cast is now guarded by an `instanceof` check with a sane fallback.
- **Ender chests can now hold ender chest items.** The "no nested containers" rule was a copy-paste of the shulker-box rule and didn't actually apply — ender chest items don't carry their own contents, so dropping ender chests into your ender chest is just storing items.

### Changes
- **Config persistence rebuilt on YACL `ConfigClassHandler`** (matches the rest of the Handy suite). On-disk JSON shape is unchanged; users without YACL installed still run on defaults, and dedicated servers no longer touch any YACL classes at all.
- **CI release workflow fixed for prerelease tags** — the grep that detects `-beta`/`-alpha`/`-rc` was failing silently because the regex started with `-`, which both GNU and BSD grep parse as a flag list. The CurseForge upload step is also now gated to stable releases (snapshot betas only ship to Modrinth + GitHub Releases until Overwolf catalogs the MC version).

### Internal
- Cleanup wave aligned this mod with the rest of the suite — JAVA_25 mixin compatibility level, full `@At` descriptors on every inject, FastUtil `Int2IntMap` on the per-slot selection hot path, deduplicated insert/remove algorithms via a new `SlotAccessor` interface, mixin renames to reflect their `Item.class` target, encapsulated mutable globals, narrowed exception handlers, named magic numbers, and a `Math.clamp` swap for the ender-glow color sweep.

## 2.1.0-beta.1

- Preview build for Minecraft **26.2 snapshots** (tested against 26.2-snapshot-3)
- Rebuilt against Fabric API 0.146.1+26.2
- Adapted `MouseHandlerMixin` to the 26.2 screen refactor: `Minecraft.screen` moved onto `Gui`, so the scroll hook now reads `minecraft.gui.screen()` when checking for an open container screen

## 2.1.0

### Bug fixes
- **Item loss on insert fixed** — inserting items into a shulker while its last slot was occupied could silently drop the new stack once the box was placed. The helper now operates on a fixed 27-slot list and fills empty positions by index instead of appending, so slot positions are preserved across insert/extract and items can no longer overflow. (fixes #4)
- **Extract no longer "walks" to the next item** — the scroll selection is now cleared after each extract, so rapid right-clicks don't drift through the shulker/ender chest in ways that were hard to predict.

### Features
- **Ender chest support** — click-to-insert, scroll-to-extract and tooltip previews now also work on ender chests. Contents are synced from the server on login and on every ender chest close so the preview is always accurate. Disabled in creative mode (server-authoritative state isn't reachable from creative slot packets); toggleable for survival via **Ender Chest Support** in the Features tab. (fixes #2)
- **Animated ender-glow tooltip border** — ender chest tooltips get a subtle cycling glow so they're visually distinct from dyed shulkers.
- **Configurable compact-mode modifier** — the compact-tooltip hotkey is no longer hard-coded to Shift. Choose Shift, Ctrl, Alt, or **None** (disables toggling entirely) under the Tooltip tab. (fixes #3)
- **Show all slots option** — new toggle to always render the full 3×9 tooltip grid including empty slots, for users who prefer a stable layout over the auto-collapsing default. (fixes #1)

## 2.0.2

- Update to Minecraft 26.1.2 compatibility
- Update Fabric Loader to 0.19.2, Fabric API to 0.146.1

## 2.0.1

- Update to Minecraft 26.1.1 compatibility
- Update Fabric Loader to 0.18.6, Fabric API to 0.145.3, YACL to 3.9.2

## 2.0.0

- Port to Minecraft 26.1 (Java 25, unobfuscated)
- Restore YACL config screen integration

## 2.0.0-beta.1

- Port to Minecraft 26.1-rc-1 (Java 25, unobfuscated)
- Restore YACL config screen integration
- Add CI/CD pipeline with automated Modrinth publishing

## v1.4.0

### Features
- **Enchantment details on hover** — selected item tooltip now shows full item info (enchantments, attributes) so you can distinguish between similar items like multiple axes with different enchantments
- **Tooltip size setting** — new Small / Medium / Large option in config to scale the tooltip grid up or down

### Improvements
- **Auto-collapsing grid** — empty trailing rows are hidden, so a shulker with a few items shows a compact 1- or 2-row grid instead of the full 3-row layout
- **Empty shulker boxes** no longer show an empty grid tooltip

## v1.3.0

### Features
- **Config screen** — 9 configurable options via YACL + ModMenu (both optional)
  - Features: toggle click-to-insert, scroll-to-extract, fullness bar, colored borders
  - Sounds: master toggle and volume slider
  - Tooltip: default compact mode, item name display, item count badges
- Config persists as `config/handyshulkers.json` (editable manually without YACL)

### Improvements
- **Shulker entity sounds** — insert/extract now use shulker open/close sounds instead of bundle sounds
- **Scroll feedback** — subtle amethyst chime on scroll selection changes
- All features fully optional and individually toggleable without game restart

## v1.2.0

### Features
- **Color-tinted tooltip border** — tooltip border matches the shulker box dye color
- **Item count badges** — selected item shows total count across all slots (e.g., "Cobblestone x204")
- **Compact mode** — hold Shift to view a condensed grid of unique items with aggregated counts, no empty slots
- Large counts abbreviated (1000+ → "1.0k", 10000+ → "10k")
- Scrolling disabled in compact mode to prevent accidental selection changes

## v1.1.0

### Improvements
- Full compatibility with ItemScroller and other MaLiLib-based mods
- Scroll events are now intercepted at the GLFW input level (MouseHandler), preventing inventory mods from moving shulker boxes while scrolling through their contents
- Existing Mouse Tweaks compatibility via Fabric screen events remains as a fallback

## v1.0.0

Initial release for Minecraft 1.21.11 (Fabric).

### Features
- Click to insert items into shulker boxes (right-click shulker on item)
- Hover to preview shulker contents in a 9x3 grid tooltip
- Scroll to select and extract items from shulker boxes
- Fullness bar showing how full a shulker box is
- Mouse Tweaks compatibility via Fabric screen event interception
- Works with all 17 shulker box colors (16 dyed + undyed)
