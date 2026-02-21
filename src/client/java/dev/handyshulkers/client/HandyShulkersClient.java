package dev.handyshulkers.client;

import dev.handyshulkers.ShulkerTooltip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Client-side initialization for Handy Shulkers.
 * Registers the ShulkerTooltip → ClientShulkerTooltip mapping
 * via Fabric's TooltipComponentCallback, and scroll event interception
 * to prevent conflicts with Mouse Tweaks and similar mods.
 */
public class HandyShulkersClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		TooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof ShulkerTooltip shulkerTooltip) {
				return new ClientShulkerTooltip(
						shulkerTooltip.items(),
						shulkerTooltip.occupiedSlots(),
						shulkerTooltip.selectedIndex()
				);
			}
			return null;
		});

		// Intercept scroll events over shulker boxes before Mouse Tweaks can process them.
		// When hovering a shulker, we handle the scroll ourselves and return false to
		// prevent mouseScrolled() from running (which blocks Mouse Tweaks' mixin).
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof AbstractContainerScreen<?>) {
				ScreenMouseEvents.allowMouseScroll(screen).register(
						(scr, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
							if (ShulkerMouseActions.handleScrollEvent(horizontalAmount, verticalAmount)) {
								return false; // Consumed — block mouseScrolled and Mouse Tweaks
							}
							return true; // Not a shulker slot — let normal processing happen
						}
				);
			}
		});
	}
}
