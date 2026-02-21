package dev.handyshulkers;

import dev.handyshulkers.config.HandyShulkersConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handy Shulkers - Gives shulker boxes bundle-like inventory interactions.
 *
 * Features:
 * - Click a shulker box on an item in your inventory to insert it (like bundles)
 * - Hover over a shulker box to see its contents, scroll to select and extract items (like bundles)
 */
public class HandyShulkers implements ModInitializer {

	public static final String MOD_ID = "handyshulkers";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		HandyShulkersConfig.load();
		LOGGER.info("Handy Shulkers loaded! Shulker boxes now behave like bundles.");
	}
}
