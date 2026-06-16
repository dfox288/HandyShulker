# Handy Shulker - Fabric Mod

## Project Overview

A Fabric mod that gives shulker boxes bundle-like inventory interactions. The mod has two core features:

1. **Click-to-insert**: Right-clicking a shulker box on an item in inventory inserts that item into the shulker (like bundles)
2. **Hover + scroll-to-extract**: Hovering over a shulker box shows its inventory, scrolling selects an item, clicking extracts it (like bundles)

## Tech Stack

- **Minecraft**: 26.2 (unobfuscated — no mappings needed)
- **Fabric Loader**: 0.19.3
- **Fabric API**: 0.152.1+26.2
- **Fabric Loom**: 1.15.5 (Gradle build plugin)
- **Java**: 25
- **YACL**: 3.9.4+26.2-fabric (soft dependency, via Modrinth Maven `maven.modrinth:yacl`)
- **ModMenu**: 20.0.0-beta.2 (soft dependency)

## Unobfuscated Minecraft (26.1+)

Minecraft 26.1 is the first **unobfuscated** version. No mappings layer is needed — no `loom.officialMojangMappings()`, no Yarn. Class/method/field names are Mojang's names directly.

Key naming conventions:
- `ItemStack`, `Player`, `Component`, `BlockItem`
- `DataComponents.CONTAINER` for accessing item container data

## Project Structure

```
HandyShulker/
├── build.gradle              # Gradle build config with Fabric Loom
├── gradle.properties         # Version configuration (MC, Fabric, Loom versions)
├── settings.gradle           # Plugin repositories
├── CLAUDE.md                 # This file
├── src/
│   ├── main/                 # Shared code (runs on BOTH client and server)
│   │   ├── java/dev/handy/mods/handyshulker/
│   │   │   ├── HandyShulker.java        # Main mod entrypoint
│   │   │   ├── ShulkerBoxHelper.java     # Utility: read/write shulker contents
│   │   │   └── mixin/
│   │   │       └── ItemShulkerInteractionMixin.java  # Click-to-insert behavior
│   │   └── resources/
│   │       ├── fabric.mod.json           # Mod descriptor
│   │       └── handyshulker.mixins.json # Mixin config (shared)
│   └── client/               # Client-only code (rendering, tooltips, input)
│       ├── java/dev/handy/mods/handyshulker/client/
│       │   ├── HandyShulkerClient.java  # Client entrypoint
│       │   └── mixin/
│       │       └── ItemShulkerTooltipMixin.java  # Tooltip + scroll behavior
│       └── resources/
│           └── handyshulker.client.mixins.json  # Client mixin config
```

**Split source sets**: The project uses `splitEnvironmentSourceSets()` in build.gradle. This means:
- `src/main/` = code that runs on both client AND server (item manipulation logic)
- `src/client/` = code that runs ONLY on the client (rendering, tooltips, mouse/scroll events)
- Never import client-only classes (like Screen, Gui, etc.) in `src/main/`

## Build Commands

```bash
cd /Users/dfox/Development/minecraft/HandyShulker && ./gradlew build
cd /Users/dfox/Development/minecraft/HandyShulker && ./gradlew runClient
cd /Users/dfox/Development/minecraft/HandyShulker && ./gradlew genSources
cd /Users/dfox/Development/minecraft/HandyShulker && ./gradlew clean
cd /Users/dfox/Development/minecraft/HandyShulker && ./gradlew --refresh-dependencies
```

**Note**: Always prefix commands with `cd /path &&` so they auto-approve via permission rules (which match on the first word of the command).

## Dependencies

YACL and ModMenu are **soft dependencies** — `compileOnly`/`localRuntime` in build.gradle. The mod works without them. Config screen code checks `FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")` at runtime.

Maven repos:
- `https://maven.terraformersmc.com/` — ModMenu
- `https://api.modrinth.com/maven` — YACL (artifact: `maven.modrinth:yacl`)

## How Mixins Work

Mixins are Fabric's way of modifying vanilla Minecraft code at runtime. They inject code into existing classes without modifying them directly.

Key concepts:
- `@Mixin(TargetClass.class)` - declares which class to modify
- `@Inject(method = "methodName", at = @At("HEAD"))` - inject at the start of a method
- `@At("RETURN")` - inject before the method returns
- `CallbackInfoReturnable<T>` - allows cancelling the original method and returning a custom value
- `cir.setReturnValue(value)` + `cir.cancel()` or just `cir.setReturnValue()` which implies cancel

Mixin method names should be prefixed with `handyshulker$` to avoid conflicts with other mods.

## Key Minecraft Classes to Reference

After running `genSources`, examine these classes (Mojang mapping names):

### For click-to-insert (Feature 1):
- `net.minecraft.world.item.BundleItem` — **THE reference implementation**. Study how it handles:
  - `overrideStackedOnOther()` — clicking bundle on other items
  - `overrideOtherStackedOnMe()` — clicking items on bundle
  - How it reads/writes `BundleContents` data component
- `net.minecraft.world.item.BlockItem` — the item class for shulker boxes
- `net.minecraft.world.item.ItemStack` — item stack manipulation
- `net.minecraft.core.component.DataComponents` — data component registry (CONTAINER is the key one)
- `net.minecraft.world.item.component.ItemContainerContents` — how shulker contents are stored

### For tooltip + scroll extraction (Feature 2):
- `net.minecraft.world.item.BundleItem.getTooltipImage()` — returns the visual tooltip
- `net.minecraft.client.gui.screens.inventory.tooltip.BundleTooltip` — bundle's tooltip renderer
- `net.minecraft.client.gui.screens.inventory.AbstractContainerScreen` — handles mouse/scroll events in inventory
- Look for scroll handling methods — bundles may use `onScrollInSlot()` or similar

### Shulker box specifics:
- `net.minecraft.world.level.block.ShulkerBoxBlock` — the block class
- Check how shulker items store their inventory: it's via `DataComponents.CONTAINER` → `ItemContainerContents`
- Shulker boxes cannot contain other shulker boxes (enforce this!)

## Implementation Strategy

### Feature 1: Click-to-insert (PARTIALLY IMPLEMENTED)
The `ItemShulkerInteractionMixin` already has the basic structure. It mixins into `Item` (not `BlockItem` — the `overrideStackedOnOther` / `overrideOtherStackedOnMe` hooks live on `Item`) and checks `isShulkerBox`/`isEnderChest` to early-exit on irrelevant items. The `ShulkerBoxHelper` and `EnderChestHelper` utilities handle the actual insertion logic; both delegate to the shared `SlotOps` algorithms via a `SlotAccessor` adapter.

**What needs verification/completion:**
- Verify method signatures match 1.21.11's actual `BlockItem` class
- Test that `ItemContainerContents.fromItems()` works correctly for saving
- Add sound effects matching vanilla bundle sounds
- Handle edge cases (full shulker, unstackable items, etc.)

### Feature 2: Tooltip + scroll-to-extract (NEEDS IMPLEMENTATION)
This is the more complex feature. Approach options:

**Option A: Reuse BundleTooltip**
- Convert shulker contents to a format BundleTooltip can render
- Pro: reuses existing vanilla rendering code
- Con: may not look right (bundle tooltip shows a different layout)

**Option B: Custom tooltip (PREFERRED)**
- Shulker boxes already show their contents in a 9x3 grid tooltip in vanilla
- We need to ADD scroll-to-select behavior on top of this existing tooltip
- Mixin into the tooltip rendering to add a selection highlight
- Mixin into scroll events to cycle selection

**Option C: Hook into AbstractContainerScreen**
- Mixin into `mouseScrolled` to detect scroll over shulker box slots
- Track which item is "selected" in the shulker
- On right-click extract, pull out the selected item

Study how `BundleItem` handles `onScrollInSlot()` (if this method exists in 1.21.11) or how the inventory screen routes scroll events to items.

## Common Pitfalls

1. **Don't import client classes in main source set** — this will crash dedicated servers
2. **Mixin target method signatures must exactly match** — if a method doesn't exist or has a different signature, the game won't load. Use `genSources` to verify.
3. **ItemContainerContents is immutable** — you must create a new instance and set it back on the stack
4. **Shulker boxes in shulker boxes** — vanilla prevents this, make sure we do too (`ShulkerBoxHelper.canInsert()`)
5. **Stack count check** — shulker boxes don't stack (max 1), always verify `stack.getCount() == 1`
6. **Play sounds** — bundle interactions play sounds; we should too for good UX

## Testing Checklist

- [ ] Insert a single item into an empty shulker box
- [ ] Insert a stackable item that partially fills a slot
- [ ] Insert items until the shulker is full (27 slots)
- [ ] Verify shulker boxes cannot be inserted into shulker boxes
- [ ] Insert into a shulker that already has some contents
- [ ] Verify insertion works with right-click (not left-click)
- [ ] Tooltip shows shulker contents on hover
- [ ] Scroll cycles through items in tooltip
- [ ] Extract selected item via scroll + click
- [ ] Test with all 16 colored shulker boxes + undyed
- [ ] Test on a dedicated server (no client crash)
- [ ] Test with other mods if possible

## Code Style

- Use `handyshulker$` prefix for all mixin method names
- Keep logic in `ShulkerBoxHelper` for reusability
- Comment complex Minecraft interactions — the codebase has unique patterns
- Use SLF4J logger from `HandyShulker.LOGGER` for debug output


<claude-mem-context>
# Recent Activity

<!-- This section is auto-generated by claude-mem. Edit content outside the tags. -->

### Feb 21, 2026

| ID | Time | T | Title | Read |
|----|------|---|-------|------|
| #27987 | 12:29 PM | 🔵 | Gradle and Fabric Loom version compatibility gap identified | ~229 |
</claude-mem-context>