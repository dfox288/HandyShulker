package dev.handyshulkers.mixin;

import dev.handyshulkers.ShulkerBoxHelper;
import dev.handyshulkers.ShulkerSelectionManager;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Mixin into Item to add bundle-like click behavior for shulker boxes.
 * We target Item because overrideStackedOnOther/overrideOtherStackedOnMe
 * are defined on Item, not BlockItem.
 *
 * Interactions (mirroring bundle behavior):
 * - Left-click shulker ON item → insert item into shulker
 * - Left-click item ON shulker → insert item into shulker
 * - Right-click shulker ON empty slot → extract selected item from shulker
 * - Right-click empty cursor ON shulker → extract selected item from shulker
 */
@Mixin(Item.class)
public abstract class ShulkerBoxItemMixin {

	/**
	 * Called when this item is clicked on another item in inventory.
	 * If this item is a shulker box:
	 * - LEFT-click on an item → insert that item into the shulker
	 * - RIGHT-click on an empty slot → extract the selected (or first) item
	 */
	@Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
	private void handyshulkers$onStackedOnOther(
			ItemStack shulkerStack, Slot slot, ClickAction action, Player player,
			CallbackInfoReturnable<Boolean> cir) {

		if (!ShulkerBoxHelper.isShulkerBox(shulkerStack) || shulkerStack.getCount() != 1) {
			return;
		}

		ItemStack targetStack = slot.getItem();

		if (action == ClickAction.PRIMARY && !targetStack.isEmpty()) {
			if (ShulkerBoxHelper.canInsert(targetStack)) {
				int inserted = ShulkerBoxHelper.tryInsert(shulkerStack, targetStack);
				if (inserted > 0) {
					playInsertSound(player);
				} else {
					playInsertFailSound(player);
				}
				cir.setReturnValue(true);
			}
		} else if (action == ClickAction.SECONDARY && targetStack.isEmpty()) {
			int extractIndex = handyshulkers$getExtractIndex(player, slot.index, shulkerStack);
			ItemStack extracted = ShulkerBoxHelper.removeOneStack(shulkerStack, extractIndex);
			if (!extracted.isEmpty()) {
				ItemStack remainder = slot.safeInsert(extracted);
				if (!remainder.isEmpty()) {
					ShulkerBoxHelper.tryInsert(shulkerStack, remainder);
				} else {
					playRemoveSound(player);
				}
				cir.setReturnValue(true);
			}
		}
	}

	/**
	 * Called when another item is clicked ON this item.
	 * If this item is a shulker box:
	 * - LEFT-click with an item → insert that item into the shulker
	 * - RIGHT-click with empty hand → extract the selected (or first) item
	 */
	@Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
	private void handyshulkers$onOtherStackedOnMe(
			ItemStack shulkerStack, ItemStack incomingStack, Slot slot, ClickAction action,
			Player player, SlotAccess slotAccess,
			CallbackInfoReturnable<Boolean> cir) {

		if (!ShulkerBoxHelper.isShulkerBox(shulkerStack) || shulkerStack.getCount() != 1) {
			return;
		}

		if (action == ClickAction.PRIMARY && !incomingStack.isEmpty()) {
			if (ShulkerBoxHelper.canInsert(incomingStack)) {
				int inserted = ShulkerBoxHelper.tryInsert(shulkerStack, incomingStack);
				if (inserted > 0) {
					playInsertSound(player);
				} else {
					playInsertFailSound(player);
				}
				cir.setReturnValue(true);
			}
		} else if (action == ClickAction.SECONDARY && incomingStack.isEmpty()) {
			if (slot.allowModification(player)) {
				int extractIndex = handyshulkers$getExtractIndex(player, slot.index, shulkerStack);
				ItemStack extracted = ShulkerBoxHelper.removeOneStack(shulkerStack, extractIndex);
				if (!extracted.isEmpty()) {
					playRemoveSound(player);
					slotAccess.set(extracted);
				}
			}
			cir.setReturnValue(true);
		}
	}

	/**
	 * Get the index to extract from, based on scroll selection.
	 * The selection is the index into the list of NON-EMPTY items (matching scroll behavior).
	 * We convert it to the actual contents list index.
	 */
	private static int handyshulkers$getExtractIndex(Player player, int slotIndex, ItemStack shulkerStack) {
		ShulkerSelectionManager manager = (ShulkerSelectionManager) player.containerMenu;
		int selectedNonEmptyIndex = manager.handyshulkers$getSelection(slotIndex);

		if (selectedNonEmptyIndex < 0) {
			// No selection — extract the first non-empty item
			return handyshulkers$firstNonEmptyIndex(shulkerStack);
		}

		// Convert non-empty item index to actual contents index
		List<ItemStack> contents = ShulkerBoxHelper.getContents(shulkerStack);
		int nonEmptyCount = 0;
		for (int i = 0; i < contents.size(); i++) {
			if (!contents.get(i).isEmpty()) {
				if (nonEmptyCount == selectedNonEmptyIndex) {
					return i;
				}
				nonEmptyCount++;
			}
		}

		// Selection out of range, fall back to first non-empty
		return handyshulkers$firstNonEmptyIndex(shulkerStack);
	}

	private static int handyshulkers$firstNonEmptyIndex(ItemStack shulkerStack) {
		List<ItemStack> contents = ShulkerBoxHelper.getContents(shulkerStack);
		for (int i = 0; i < contents.size(); i++) {
			if (!contents.get(i).isEmpty()) return i;
		}
		return 0;
	}

	// -- Fullness bar (matches bundle style) --

	private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);
	private static final int BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);

	@Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
	private void handyshulkers$isBarVisible(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (ShulkerBoxHelper.isShulkerBox(stack)) {
			cir.setReturnValue(ShulkerBoxHelper.getOccupiedSlots(stack) > 0);
		}
	}

	@Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
	private void handyshulkers$getBarWidth(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (ShulkerBoxHelper.isShulkerBox(stack)) {
			int occupied = ShulkerBoxHelper.getOccupiedSlots(stack);
			cir.setReturnValue(Math.min(1 + 12 * occupied / ShulkerBoxHelper.SHULKER_SLOTS, 13));
		}
	}

	@Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
	private void handyshulkers$getBarColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (ShulkerBoxHelper.isShulkerBox(stack)) {
			int occupied = ShulkerBoxHelper.getOccupiedSlots(stack);
			cir.setReturnValue(occupied >= ShulkerBoxHelper.SHULKER_SLOTS ? FULL_BAR_COLOR : BAR_COLOR);
		}
	}

	// -- Sound effects --

	private static void playInsertSound(Player player) {
		player.playSound(
				net.minecraft.sounds.SoundEvents.BUNDLE_INSERT,
				0.8F,
				0.8F + player.level().getRandom().nextFloat() * 0.4F
		);
	}

	private static void playInsertFailSound(Player player) {
		player.playSound(
				net.minecraft.sounds.SoundEvents.BUNDLE_INSERT_FAIL,
				1.0F, 1.0F
		);
	}

	private static void playRemoveSound(Player player) {
		player.playSound(
				net.minecraft.sounds.SoundEvents.BUNDLE_REMOVE_ONE,
				0.8F,
				0.8F + player.level().getRandom().nextFloat() * 0.4F
		);
	}
}
