package dev.handyshulkers.client.mixin;

import dev.handyshulkers.client.ShulkerMouseActions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers our ShulkerMouseActions with the inventory screen
 * so scroll events over shulker boxes are handled.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenMixin {

	@Shadow
	protected abstract void addItemSlotMouseAction(ItemSlotMouseAction action);

	@Inject(method = "init", at = @At("TAIL"))
	private void handyshulkers$onInit(CallbackInfo ci) {
		addItemSlotMouseAction(new ShulkerMouseActions(Minecraft.getInstance()));
	}
}
