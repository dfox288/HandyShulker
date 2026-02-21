package dev.handyshulkers.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.handyshulkers.config.HandyShulkersConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

/**
 * Shared client-side utilities for shulker tooltip behavior.
 */
@Environment(EnvType.CLIENT)
public final class ShulkerClientUtil {

	private ShulkerClientUtil() {}

	/**
	 * Determines if compact mode should be active.
	 * XOR logic: if defaultCompactMode is true, Shift toggles to grid mode instead.
	 */
	public static boolean isCompactMode() {
		boolean shiftHeld = InputConstants.isKeyDown(
				Minecraft.getInstance().getWindow(),
				org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT
		) || InputConstants.isKeyDown(
				Minecraft.getInstance().getWindow(),
				org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT
		);
		return shiftHeld ^ HandyShulkersConfig.get().defaultCompactMode;
	}
}
