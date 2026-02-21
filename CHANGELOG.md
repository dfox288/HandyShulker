# Changelog

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
