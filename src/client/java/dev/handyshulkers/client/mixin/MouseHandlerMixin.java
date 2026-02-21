package dev.handyshulkers.client.mixin;

import dev.handyshulkers.client.ShulkerMouseActions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts scroll events at the GLFW level, before MaLiLib-based mods
 * (ItemScroller, etc.) can process them. Priority 200 ensures we run
 * before MaLiLib's default priority (1000).
 */
@Mixin(value = MouseHandler.class, priority = 200)
public class MouseHandlerMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void handyshulkers$onScroll(long window, double scrollX, double scrollY, CallbackInfo ci) {
		if (!(this.minecraft.screen instanceof AbstractContainerScreen<?>)
				|| ShulkerMouseActions.lastHoveredSlotIndex < 0) {
			return;
		}

		// Apply the same sensitivity processing as vanilla MouseHandler
		boolean discrete = this.minecraft.options.discreteMouseScroll().get();
		double sensitivity = this.minecraft.options.mouseWheelSensitivity().get();
		double h = (discrete ? Math.signum(scrollX) : scrollX) * sensitivity;
		double v = (discrete ? Math.signum(scrollY) : scrollY) * sensitivity;

		if (ShulkerMouseActions.handleScrollEvent(h, v)) {
			ci.cancel();
		}
	}
}
