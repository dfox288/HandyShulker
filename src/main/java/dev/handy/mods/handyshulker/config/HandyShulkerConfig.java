package dev.handy.mods.handyshulker.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.handy.mods.handyshulker.HandyShulker;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration for Handy Shulker.
 * Persisted as JSON in config/handyshulker.json.
 * All defaults match the pre-config hardcoded behavior.
 */
public class HandyShulkerConfig {

	private static HandyShulkerConfig INSTANCE;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("handyshulker.json");
	// Legacy path from before the v2.1 mod-id rename (handyshulkers → handyshulker).
	// Read once on first load so user settings carry over; safe to remove after a few releases.
	private static final Path LEGACY_CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("handyshulkers.json");

	// -- Features --
	public boolean enableClickInsert = true;
	public boolean enableScrollExtract = true;
	public boolean enableEnderChestSupport = true;
	public boolean showFullnessBar = true;
	public boolean showColoredBorders = true;

	// -- Sounds --
	public boolean enableSounds = true;
	public float soundVolume = 1.0f;

	// -- Tooltip --
	public boolean defaultCompactMode = false;
	public CompactModeKey compactModeKey = CompactModeKey.SHIFT;
	public boolean showItemName = true;
	public boolean showItemCounts = true;
	public boolean showAllSlots = false;
	public TooltipSize tooltipSize = TooltipSize.LARGE;

	public static HandyShulkerConfig get() {
		if (INSTANCE == null) {
			load();
		}
		return INSTANCE;
	}

	public static void load() {
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

		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				INSTANCE = GSON.fromJson(reader, HandyShulkerConfig.class);
				if (INSTANCE == null) {
					INSTANCE = new HandyShulkerConfig();
				}
			} catch (Exception e) {
				HandyShulker.LOGGER.warn("Failed to load config, using defaults", e);
				INSTANCE = new HandyShulkerConfig();
			}
		} else {
			INSTANCE = new HandyShulkerConfig();
			save();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
		} catch (IOException e) {
			HandyShulker.LOGGER.warn("Failed to save config", e);
		}
	}
}
