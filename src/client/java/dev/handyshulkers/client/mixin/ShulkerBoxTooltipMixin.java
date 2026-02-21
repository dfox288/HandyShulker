package dev.handyshulkers.client.mixin;

import dev.handyshulkers.ShulkerBoxHelper;
import dev.handyshulkers.ShulkerSelectionManager;
import dev.handyshulkers.ShulkerTooltip;
import dev.handyshulkers.client.ShulkerMouseActions;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Client-side mixin that overrides getTooltipImage for shulker boxes.
 * Returns a ShulkerTooltip which Fabric's TooltipComponentCallback
 * converts into our ClientShulkerTooltip renderer.
 */
@Mixin(Item.class)
public abstract class ShulkerBoxTooltipMixin {

	@Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
	private void handyshulkers$getTooltipImage(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
		if (!ShulkerBoxHelper.isShulkerBox(stack)) {
			return;
		}

		List<ItemStack> contents = ShulkerBoxHelper.getContents(stack);

		// Pad to 27 slots so the renderer shows empty slots
		List<ItemStack> padded = new ArrayList<>(ShulkerBoxHelper.SHULKER_SLOTS);
		padded.addAll(contents);
		while (padded.size() < ShulkerBoxHelper.SHULKER_SLOTS) {
			padded.add(ItemStack.EMPTY);
		}

		int occupied = (int) contents.stream().filter(s -> !s.isEmpty()).count();

		// Read the current selection from the container menu
		int selectedIndex = -1;
		int hoveredSlot = ShulkerMouseActions.lastHoveredSlotIndex;
		if (hoveredSlot >= 0) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null && mc.player.containerMenu != null) {
				ShulkerSelectionManager manager = (ShulkerSelectionManager) mc.player.containerMenu;
				selectedIndex = manager.handyshulkers$getSelection(hoveredSlot);
			}
		}

		DyeColor color = ShulkerBoxHelper.getColor(stack);
		cir.setReturnValue(Optional.of(new ShulkerTooltip(padded, occupied, selectedIndex, color)));
	}
}
