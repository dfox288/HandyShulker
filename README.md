# Handy Shulkers

A Fabric mod for Minecraft 1.21.11 that gives shulker boxes bundle-like inventory interactions.

## Features

- **Click-to-insert**: Right-click a shulker box on an item in your inventory to insert it into the shulker — just like bundles!
- **Hover + scroll-to-extract**: Hover over a shulker box to see its contents, scroll with the mousewheel to select an item, and extract it without placing the shulker box.

## Requirements

- Minecraft Java Edition 1.21.11
- [Fabric Loader](https://fabricmc.net/use/installer/) 0.18.1+
- [Fabric API](https://modrinth.com/mod/fabric-api) 0.139.5+

## Installation

1. Install Fabric Loader for Minecraft 1.21.11
2. Download Fabric API and place it in your `mods/` folder
3. Download Handy Shulkers and place it in your `mods/` folder
4. Launch the game!

## Building from Source

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/handyshulkers.git
cd handyshulkers

# Build the mod
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
