# Handy Shulker

![Handy Shulker](handy-shulker-gallery.png)

A Fabric mod for Minecraft 1.21.11 that makes shulker boxes work like bundles.

Click to insert items, hover to preview contents, and scroll to extract — all without placing the box down.

[![Modrinth](https://img.shields.io/badge/Modrinth-handy--shulker-green)](https://modrinth.com/project/handy-shulker)

## Features

- **Click to insert** — Right-click a shulker box on an item in your inventory to insert it, just like bundles.
- **Hover to preview** — Hover over a shulker box to see its contents in a 9x3 grid tooltip.
- **Scroll to extract** — Scroll to select an item in the tooltip, then click to extract it.
- **Fullness bar** — Shulker boxes show a colored bar indicating how full they are.
- **Mouse Tweaks compatible** — Works cleanly alongside Mouse Tweaks and other inventory mods.

## Requirements

- Minecraft Java Edition 1.21.11
- [Fabric Loader](https://fabricmc.net/use/installer/) 0.18.1+
- [Fabric API](https://modrinth.com/mod/fabric-api) 0.139.5+

## Installation

1. Install Fabric Loader for Minecraft 1.21.11
2. Download Fabric API and place it in your `mods/` folder
3. Download Handy Shulker and place it in your `mods/` folder
4. Launch the game!

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

## License

MIT License — see [LICENSE](LICENSE) for details.
