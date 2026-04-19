package dev.handyshulkers.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import dev.handyshulkers.config.CompactModeKey;
import dev.handyshulkers.config.HandyShulkersConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Shared client-side utilities for shulker tooltip behavior.
 */
@Environment(EnvType.CLIENT)
public final class ShulkerClientUtil {

	private ShulkerClientUtil() {}

	/**
	 * Determines if compact mode should be active.
	 * XOR with defaultCompactMode so holding the modifier toggles the view.
	 * If compactModeKey == NONE, only defaultCompactMode decides.
	 */
	public static boolean isCompactMode() {
		return modifierHeld() ^ HandyShulkersConfig.get().defaultCompactMode;
	}

	private static boolean modifierHeld() {
		CompactModeKey key = HandyShulkersConfig.get().compactModeKey;
		if (key == null || key == CompactModeKey.NONE) return false;

		Window window = Minecraft.getInstance().getWindow();
		return switch (key) {
			case SHIFT -> InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
					|| InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
			case CTRL -> InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)
					|| InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
			case ALT -> InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT)
					|| InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT);
			default -> false;
		};
	}
}
