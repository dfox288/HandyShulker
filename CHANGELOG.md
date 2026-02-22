# Changelog

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
