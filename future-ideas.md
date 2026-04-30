# Handy Shulker - Future Feature Ideas

Proposals from two independent analyses of the codebase (Feb 2026).

---

## High Priority (Both Specialists Agree)

### Quick Dump — Bulk Insert via Shift-Click
**Complexity: Medium**

Shift+right-click a shulker box on an item to insert ALL matching items from your inventory into it, not just one stack. If you have 5 stacks of cobblestone scattered across your inventory, they all get vacuumed in (space permitting).

- Extend `ItemShulkerInteractionMixin.handyshulker$onStackedOnOther()` to detect shift via `player.isShiftKeyDown()`
- Iterate `player.getInventory().items` for `isSameItemSameComponents` matches, call `tryInsert` in a loop
- Server-safe: `isShiftKeyDown()` is server-authoritative, no custom packets needed
- Add config toggle `enableBulkInsert`
- Consider a "chunkier" sound for bulk vs single insert

**Why**: Most-requested feature across all shulker mods. Easy Shulker and Shulker+ have versions but neither integrates into bundle-style interaction.

---

### Ghost Slots — Insertion Preview + Extraction Memory
**Complexity: Easy-Medium**

Two complementary behaviors:

1. **Before insertion**: When holding an item over a shulker, the tooltip highlights merge-target slots (green) and the first available empty slot (blue). Full shulker flashes red.
2. **After extraction**: When you extract an item, the now-empty slot briefly shows a fading ghost of the removed item (~1.5s).

**Insertion preview implementation:**
- Detect carried item via `player.containerMenu.getCarried()` in tooltip rendering
- Pass carried item into `ShulkerTooltip`, render overlays in `ClientShulkerTooltip.renderGrid()`
- Use `ItemStack.isSameItemSameComponents()` for merge matching

**Extraction ghost implementation:**
- Client-side `Map<Integer, GhostSlot>` with item + timestamp
- Render with reduced alpha (`guiGraphics.setColor(1, 1, 1, alpha)`) for slots with ghosts < 1500ms old
- Entirely client-side, zero server impact

**Why**: Pure UX polish that makes the mod feel premium. Vanilla bundles don't even do this.

---

### Shulker Search — Type-to-Filter Inside Tooltips
**Complexity: Hard**

While hovering over a shulker tooltip, start typing to filter/highlight items by name. Type "dia" and only diamond items light up; everything else dims. Escape or moving away clears the filter.

- Client-side mixin to capture `keyPressed` while tooltip is visible
- Store search string in `ShulkerMouseActions` or new `ShulkerSearchState`
- Apply darkening overlay to non-matching slots in `ClientShulkerTooltip.renderGrid()`
- Match via `stack.getHoverName().getString().toLowerCase().contains(filter)`
- Debounce search (100-200ms) for performance with many shulkers
- Entirely client-side, zero server impact
- Consider regex/tag search for power users (e.g., `#logs`)

**Why**: No existing shulker mod does this. Genuinely novel. Transformative for players with 15+ shulkers of assorted loot.

---

### Slot Lock — Prevent Accidental Extraction
**Complexity: Medium-Hard**

Ctrl+click (or middle-click) on a shulker to toggle "locked" mode. Locked shulkers allow insertion but prevent scroll-extract. Small padlock icon overlay on the item in inventory. Lock state persists with the item.

- Store lock as custom data component on the shulker ItemStack (or `DataComponents.CUSTOM_DATA` compound tag)
- Check lock flag in extraction path before allowing `removeOneStack`
- Render padlock overlay via `AbstractContainerScreen.renderSlot()` mixin
- Data component approach works seamlessly in multiplayer (travels with item)
- Config toggle `enableSlotLock`

**Why**: Safety net that makes power features trustworthy. Prevents accidental extraction of Elytra, Totems, etc. No competing mod offers per-shulker locking.

---

## Medium Priority

### Stack Squash — Auto-Consolidate Fragmented Stacks
**Complexity: Easy**

Double-right-click a shulker in inventory to merge fragmented stacks (three stacks of 21 cobblestone become one stack of 63) and sort contents contiguously.

- Add `ShulkerBoxHelper.consolidate(ItemStack shulkerStack)`: read contents, group by `isSameItemSameComponents`, merge respecting `getMaxStackSize()`, remove empties, write back
- Trigger: detect double-right-click (track last click time, fire on second click within 300ms with empty cursor)
- Configurable sort order: by item ID, by count, by name
- Server-safe, operates on DataComponents

**Why**: Click-to-insert over time creates messy fragmented stacks. This is the cleanup tool that completes the workflow. No shulker mod does in-inventory consolidation.

---

### Smart Sort — One-Click Shulker Organization
**Complexity: Medium**

Middle-click a shulker in inventory to sort its contents by type, count, or creative-inventory order. Configurable sort mode.

- Add `ShulkerBoxHelper.sortContents(ItemStack, SortMode)` with `SortMode` enum: `BY_NAME`, `BY_COUNT`, `BY_ID`
- Sorting reads contents, sorts the list, calls `setContents()`
- Middle-click detection needs `ContainerScreen` mixin (not a vanilla `ClickAction`)
- Only activate in survival mode on shulker boxes (middle-click is "pick block" in creative)

**Why**: Unique differentiator. Inventory sorting mods sort your inventory, not individual container items. Power-user catnip.

---

### Color Peek — Preview Without Opening Inventory
**Complexity: Hard**

**Variant A (Hotbar):** Hold Alt to see shulker contents from the hotbar without opening inventory. Requires keybind registration, HUD mixin, and manual tooltip rendering outside `AbstractContainerScreen`.

**Variant B (Placed blocks):** Sneak+look at a placed shulker to preview contents. Requires crosshair detection and block entity data reading.

- Variant A is entirely client-side
- Variant B has server-sync concerns: placed shulker contents aren't synced to client by default (only when opened)
- Cache tooltip data to avoid per-frame `getContents()` overhead
- Potential conflicts with Jade/WTHIT overlay mods

**Why**: The killer feature of ShulkerBoxTooltip (most downloaded shulker mod). Non-negotiable for being a one-stop replacement.

---

## Lower Priority / Experimental

### Stack Dump — Quick Transfer to Containers
**Complexity: Hard**

Shift+right-click a shulker on a chest/barrel to dump all contents into that container. Alternative: dump contents as item entities on the ground.

- Mixin into `BlockItem.useOn()` to intercept placement when target is a Container
- "Dump to ground" variant needs server config (potential lag/grief vector)
- Philosophically different from "bundles in inventory" core — may belong in a companion mod

**Why**: Collapses the place-open-shift-click-27-times-break-pickup workflow into one action.

---

### Shulker Hotbar — Extract From Gameplay
**Complexity: Hard**

Keybind to open a compact overlay showing held shulker's contents. Scroll to select, press again to extract directly to hand. No inventory screen needed.

- Register keybind via `KeyBindingHelper`
- Render custom Screen or HUD overlay with compact grid
- Extraction via existing `ServerboundSelectBundleItemPacket` path
- Needs anti-cheat plugin testing (Vulcan, Grim, Matrix) for competitive servers
- Start with Screen approach (simpler), consider HUD overlay later

**Why**: The endgame for shulker QoL. Players who benefit most from fast access (builders, PvP, farmers) are the ones who least want to open inventory.

---

## Implementation Notes

- The `ShulkerBoxHelper` utility class is the right place for consolidation, bulk-insert, and sort logic
- `ClientShulkerTooltip` renderer has clean separation for ghost slots and search highlighting
- `ShulkerMouseActions` already handles the scroll state machine, extendable for search input
- Config system (YACL + ModMenu + JSON fallback) is ready for new toggles
- All features should have individual config toggles defaulting to enabled
