package dev.handy.mods.handyshulker.config;

import dev.handy.mods.handyshulker.HandyShulker;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HandyShulkerConfig {

	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve(HandyShulker.MOD_ID + ".json");
	// Legacy path from before the v2.1 mod-id rename (handyshulkers → handyshulker).
	// Migrated once on first load so user settings carry over; safe to remove after a few releases.
	private static final Path LEGACY_CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("handyshulkers.json");

	private static HandyShulkerConfig INSTANCE;

	// -- Features --
	@SerialEntry public boolean enableClickInsert = true;
	@SerialEntry public boolean enableScrollExtract = true;
	@SerialEntry public boolean enableEnderChestSupport = true;
	@SerialEntry public boolean showFullnessBar = true;
	@SerialEntry public boolean showColoredBorders = true;

	// -- Sounds --
	@SerialEntry public boolean enableSounds = true;
	@SerialEntry public float soundVolume = 1.0f;

	// -- Tooltip --
	@SerialEntry public boolean defaultCompactMode = false;
	@SerialEntry public CompactModeKey compactModeKey = CompactModeKey.SHIFT;
	@SerialEntry public boolean showItemName = true;
	@SerialEntry public boolean showItemCounts = true;
	@SerialEntry public boolean showAllSlots = false;
	@SerialEntry public TooltipSize tooltipSize = TooltipSize.LARGE;

	public HandyShulkerConfig() {}

	public static HandyShulkerConfig get() {
		if (INSTANCE == null) {
			load();
		}
		return INSTANCE;
	}

	public static void load() {
		migrateLegacyConfigIfNeeded();
		if (yaclLoaded()) {
			YaclStorage.HANDLER.load();
			INSTANCE = YaclStorage.HANDLER.instance();
		} else {
			// Without YACL, the config screen can't be opened anyway — run with defaults.
			// Any previously persisted config sits on disk and gets picked up the moment YACL is installed.
			INSTANCE = new HandyShulkerConfig();
		}
	}

	public static void save() {
		if (yaclLoaded()) {
			YaclStorage.HANDLER.save();
		}
	}

	private static boolean yaclLoaded() {
		return FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3");
	}

	private static void migrateLegacyConfigIfNeeded() {
		if (!Files.exists(CONFIG_PATH) && Files.exists(LEGACY_CONFIG_PATH)) {
			try {
				Files.copy(LEGACY_CONFIG_PATH, CONFIG_PATH);
				HandyShulker.LOGGER.info("Migrated config from {} to {}",
						LEGACY_CONFIG_PATH.getFileName(), CONFIG_PATH.getFileName());
			} catch (IOException e) {
				HandyShulker.LOGGER.warn("Failed to migrate legacy config from {}",
						LEGACY_CONFIG_PATH.getFileName(), e);
			}
		}
	}

	/**
	 * Holds the YACL handler. Inner-class loading is lazy in the JVM, so this class is only
	 * resolved when {@link #yaclLoaded()} is true and we actually reference it. That keeps
	 * YACL classes off the always-executed code path and avoids a {@code NoClassDefFoundError}
	 * for users (or dedicated servers) running without YACL installed.
	 */
	private static final class YaclStorage {
		static final ConfigClassHandler<HandyShulkerConfig> HANDLER =
				ConfigClassHandler.createBuilder(HandyShulkerConfig.class)
						.id(Identifier.fromNamespaceAndPath(HandyShulker.MOD_ID, "config"))
						.serializer(cfg -> GsonConfigSerializerBuilder.create(cfg)
								.setPath(CONFIG_PATH)
								.build())
						.build();
	}
}
