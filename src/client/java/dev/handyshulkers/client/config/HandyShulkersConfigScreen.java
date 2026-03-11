package dev.handyshulkers.client.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

/**
 * Config screen placeholder — YACL is not yet available for Minecraft 26.1.
 * Config can still be edited manually via config/handyshulkers.json.
 *
 * Once YACL is ported to 26.1, this class should be restored to use the
 * YACL builder API. See git history for the full YACL implementation.
 */
@Environment(EnvType.CLIENT)
public class HandyShulkersConfigScreen {

	public static Screen create(Screen parent) {
		// YACL not available for 26.1 yet — return parent screen
		return parent;
	}
}
