package dev.handy.mods.handyshulker.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.handy.mods.handyshulker.config.CompactModeKey;
import dev.handy.mods.handyshulker.config.HandyShulkerConfig;

/**
 * Shared client-side utilities for shulker tooltip behavior.
 */
public final class ShulkerClientUtil {

	private ShulkerClientUtil() {}

	/**
	 * Determines if compact mode should be active.
	 * XOR with defaultCompactMode so holding the modifier toggles the view.
	 * If compactModeKey == NONE, only defaultCompactMode decides.
	 */
	public static boolean isCompactMode() {
		return modifierHeld() ^ HandyShulkerConfig.get().defaultCompactMode;
	}

	private static boolean modifierHeld() {
		CompactModeKey key = HandyShulkerConfig.get().compactModeKey;
		if (key == null || key == CompactModeKey.NONE) return false;

		return switch (key) {
			case SHIFT -> InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
					|| InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
			case CTRL -> InputConstants.isKeyDown(InputConstants.KEY_LCONTROL)
					|| InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);
			case ALT -> InputConstants.isKeyDown(InputConstants.KEY_LALT)
					|| InputConstants.isKeyDown(InputConstants.KEY_RALT);
			default -> false;
		};
	}
}
