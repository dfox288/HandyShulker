package dev.handy.mods.handyshulker.client.mixin;

import dev.handy.mods.handyshulker.EnderChestHelper;
import dev.handy.mods.handyshulker.HandyContainers;
import dev.handy.mods.handyshulker.ShulkerBoxHelper;
import dev.handy.mods.handyshulker.ShulkerSelectionManager;
import dev.handy.mods.handyshulker.ShulkerTooltip;
import dev.handy.mods.handyshulker.client.ShulkerMouseActions;
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
public abstract class ItemShulkerTooltipMixin {

	@Inject(method = "getTooltipImage(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
	private void handyshulker$getTooltipImage(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
		if (!HandyContainers.isSupported(stack)) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		List<ItemStack> contents = HandyContainers.getContents(stack, mc.player);
		if (contents.isEmpty()) return;

		// Pad to 27 slots so the renderer shows empty slots
		List<ItemStack> padded = new ArrayList<>(ShulkerBoxHelper.SHULKER_SLOTS);
		padded.addAll(contents);
		while (padded.size() < ShulkerBoxHelper.SHULKER_SLOTS) {
			padded.add(ItemStack.EMPTY);
		}

		int occupied = (int) contents.stream().filter(s -> !s.isEmpty()).count();

		// Read the current selection from the container menu
		int selectedIndex = -1;
		int hoveredSlot = ShulkerMouseActions.currentHoveredSlot();
		if (hoveredSlot >= 0 && mc.player != null && mc.player.containerMenu != null) {
			ShulkerSelectionManager manager = (ShulkerSelectionManager) mc.player.containerMenu;
			selectedIndex = manager.handyshulker$getSelection(hoveredSlot);
		}

		DyeColor color = HandyContainers.getColor(stack);
		boolean isEnderChest = EnderChestHelper.isEnderChest(stack);
		cir.setReturnValue(Optional.of(new ShulkerTooltip(padded, occupied, selectedIndex, color, isEnderChest)));
	}
}
