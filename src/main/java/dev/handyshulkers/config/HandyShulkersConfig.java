package dev.handyshulkers.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.handyshulkers.HandyShulkers;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration for Handy Shulkers.
 * Persisted as JSON in config/handyshulkers.json.
 * All defaults match the pre-config hardcoded behavior.
 */
public class HandyShulkersConfig {

	private static HandyShulkersConfig INSTANCE;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("handyshulkers.json");

	// -- Features --
	public boolean enableClickInsert = true;
	public boolean enableScrollExtract = true;
	public boolean showFullnessBar = true;
	public boolean showColoredBorders = true;

	// -- Sounds --
	public boolean enableSounds = true;
	public float soundVolume = 1.0f;

	// -- Tooltip --
	public boolean defaultCompactMode = false;
	public boolean showItemName = true;
	public boolean showItemCounts = true;

	public static HandyShulkersConfig get() {
		if (INSTANCE == null) {
			load();
		}
		return INSTANCE;
	}

	public static void load() {
		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				INSTANCE = GSON.fromJson(reader, HandyShulkersConfig.class);
				if (INSTANCE == null) {
					INSTANCE = new HandyShulkersConfig();
				}
			} catch (Exception e) {
				HandyShulkers.LOGGER.warn("Failed to load config, using defaults", e);
				INSTANCE = new HandyShulkersConfig();
			}
		} else {
			INSTANCE = new HandyShulkersConfig();
			save();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
		} catch (IOException e) {
			HandyShulkers.LOGGER.warn("Failed to save config", e);
		}
	}
}
