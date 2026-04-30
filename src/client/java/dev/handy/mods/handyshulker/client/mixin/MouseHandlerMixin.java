package dev.handy.mods.handyshulker.client.mixin;

import dev.handy.mods.handyshulker.client.ShulkerMouseActions;
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
 * (ItemScroller, etc.) can process them. We must apply our injection
 * before MaLiLib's, otherwise their cancellable injection eats the event
 * and ours never fires when hovering a shulker.
 *
 * MaLiLib uses Mixin's default priority (1000); anything below 1000 runs
 * earlier in the apply order. 200 leaves headroom for any mod that wants
 * to slot in between us and the floor (priority 0) without changing this
 * value.
 */
// Annotation arguments must be compile-time constants resolvable at the
// annotation site, and Java forbids forward-referencing same-class fields
// from a class-level annotation, so the priority literal stays inline. The
// `200` here is "before MaLiLib (default 1000)" — see class doc above.
@Mixin(value = MouseHandler.class, priority = 200)
public class MouseHandlerMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "onScroll(JDD)V", at = @At("HEAD"), cancellable = true)
	private void handyshulker$onScroll(long window, double scrollX, double scrollY, CallbackInfo ci) {
		if (!(this.minecraft.gui.screen() instanceof AbstractContainerScreen<?>)
				|| ShulkerMouseActions.currentHoveredSlot() < 0) {
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
