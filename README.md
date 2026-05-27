# Handy Shulker

![Handy Shulker](handy-shulker-gallery.png)

A Fabric mod that makes shulker boxes work like bundles. Stable builds target Minecraft 26.1.x; the **beta channel** tracks the 26.2 prerelease cycle (currently 26.2-pre-1).

Click to insert items, hover to preview contents, and scroll to extract — all without placing the box down.

[![Modrinth](https://img.shields.io/badge/Modrinth-handy--shulker-green)](https://modrinth.com/project/handy-shulker)

## Features

- **Click to insert** — Right-click a shulker box on an item in your inventory to insert it, just like bundles.
- **Hover to preview** — Hover over a shulker box to see its contents in a 9x3 grid tooltip.
- **Scroll to extract** — Scroll to select an item in the tooltip, then click to extract it.
- **Fullness bar** — Shulker boxes show a colored bar indicating how full they are.
- **Configurable** — All features individually toggleable via config screen or JSON file.
- **Mouse Tweaks compatible** — Works cleanly alongside Mouse Tweaks and other inventory mods.

## Requirements

- Minecraft Java Edition 26.1.x (stable) or 26.2 prerelease (beta channel)
- [Fabric Loader](https://fabricmc.net/use/installer/) 0.19.2+
- [Fabric API](https://modrinth.com/mod/fabric-api) matching your Minecraft version

### Optional (for config screen)

- [ModMenu](https://modrinth.com/mod/modmenu) — adds a Configure button in the mod list
- [YACL](https://modrinth.com/mod/yacl) — powers the in-game config screen

Without these, all features work with sensible defaults. You can also edit `config/handyshulker.json` manually.

## Installation

### Single Player

1. Install Fabric Loader for Minecraft 26.1.x (or 26.2 prerelease, if you're using the beta build)
2. Download Fabric API and place it in your `mods/` folder
3. Download Handy Shulker and place it in your `mods/` folder
4. Launch the game!

### Server

The mod is required on **both the server and all connecting clients**.

**Server setup:**
1. Install Fabric Loader on your server
2. Place Fabric API and Handy Shulker in the server's `mods/` folder
3. Start the server

**Client setup:**
1. Each player needs Fabric Loader, Fabric API, and Handy Shulker installed
2. Players without the mod will not be able to use shulker box interactions correctly

## Building from Source

```bash
git clone https://github.com/dfox288/HandyShulker.git
cd HandyShulker

./gradlew build
# The compiled JAR will be in build/libs/
```

## Development

```bash
# Generate Minecraft sources for reference
./gradlew genSources

# Run Minecraft with the mod loaded
./gradlew runClient
```

## Part of the Handy series

Small Fabric mods that smooth over vanilla friction points:

- [Handy Bookshelf](https://modrinth.com/mod/handy-bookshelf) — enchantment glint and name tags for chiseled bookshelves
- [Handy Trader](https://modrinth.com/mod/handy-trader) — bookmark your favorite villager trades
- [Handy Indicator](https://modrinth.com/mod/handy-indicator) — visual indicators on container blocks

## License

MIT License — see [LICENSE](LICENSE) for details.
