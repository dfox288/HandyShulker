package dev.handy.mods.handyshulker.mixin;

import dev.handy.mods.handyshulker.HandyContainers;
import dev.handy.mods.handyshulker.ShulkerSelectionManager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Injects ShulkerSelectionManager interface into AbstractContainerMenu.
 * This stores per-slot selected item indices for shulker boxes.
 *
 * Also intercepts setSelectedBundleItemIndex to handle the bundle selection
 * packet for shulker boxes (the client reuses this packet for scroll selection).
 */
@Mixin(AbstractContainerMenu.class)
public abstract class ContainerMenuMixin implements ShulkerSelectionManager {

	@Shadow
	@Final
	public NonNullList<Slot> slots;

	@Unique
	private final Map<Integer, Integer> handyshulker$selections = new HashMap<>();

	@Override
	public int handyshulker$getSelection(int slotIndex) {
		return handyshulker$selections.getOrDefault(slotIndex, -1);
	}

	@Override
	public void handyshulker$setSelection(int slotIndex, int selectedItemIndex) {
		if (selectedItemIndex < 0) {
			handyshulker$selections.remove(slotIndex);
		} else {
			handyshulker$selections.put(slotIndex, selectedItemIndex);
		}
	}

	@Override
	public void handyshulker$clearSelection(int slotIndex) {
		handyshulker$selections.remove(slotIndex);
	}

	/**
	 * Intercept the bundle selection packet handler. When the slot contains a
	 * shulker box instead of a bundle, store the selection in our map.
	 */
	@Inject(method = "setSelectedBundleItemIndex(II)V", at = @At("HEAD"))
	private void handyshulker$onSetSelectedBundleItemIndex(int slotId, int selectedItemIndex, CallbackInfo ci) {
		if (slotId >= 0 && slotId < slots.size()) {
			ItemStack stack = slots.get(slotId).getItem();
			if (HandyContainers.isSupported(stack)) {
				handyshulker$setSelection(slotId, selectedItemIndex);
			}
		}
	}
}
