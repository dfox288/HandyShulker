package dev.handy.mods.handyshulker.mixin;

import dev.handy.mods.handyshulker.HandyContainers;
import dev.handy.mods.handyshulker.ShulkerBoxHelper;
import dev.handy.mods.handyshulker.ShulkerSelectionManager;
import dev.handy.mods.handyshulker.config.HandyShulkerConfig;
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
 *
 * <h2>Why Item, not BlockItem?</h2>
 * The two click hooks we need ({@code overrideStackedOnOther} and
 * {@code overrideOtherStackedOnMe}) are declared on {@code Item}, not on
 * {@code BlockItem}. Mixin can only inject into the class that <em>declares</em>
 * a method, so targeting BlockItem would resolve as "method not found." The
 * cost of targeting Item is that our injected bytecode loads on every Item
 * subclass and our handlers run for every {@code overrideStackedOnOther} /
 * {@code overrideOtherStackedOnMe} invocation in the entire game. The
 * {@code isShulkerBox(stack)} / {@code isEnderChest(stack)} early-exit at the
 * top of each handler bails out in O(1) for non-container items, which is
 * cheap enough that the trade-off is fine — but if you're hunting perf
 * regressions in inventory click paths, this is one place to look.
 *
 * <h2>Interactions (mirroring bundle behavior):</h2>
 * <ul>
 * <li>Left-click shulker ON item → insert item into shulker
 * <li>Left-click item ON shulker → insert item into shulker
 * <li>Right-click shulker ON empty slot → extract selected item from shulker
 * <li>Right-click empty cursor ON shulker → extract selected item from shulker
 * </ul>
 */
@Mixin(Item.class)
public abstract class ShulkerBoxItemMixin {

	/**
	 * Called when this item is clicked on another item in inventory.
	 * If this item is a shulker box:
	 * - LEFT-click on an item → insert that item into the shulker
	 * - RIGHT-click on an empty slot → extract the selected (or first) item
	 */
	@Inject(method = "overrideStackedOnOther(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/inventory/ClickAction;Lnet/minecraft/world/entity/player/Player;)Z", at = @At("HEAD"), cancellable = true)
	private void handyshulker$onStackedOnOther(
			ItemStack containerStack, Slot slot, ClickAction action, Player player,
			CallbackInfoReturnable<Boolean> cir) {

		if (!HandyContainers.isActionAllowed(containerStack, player) || containerStack.getCount() != 1) {
			return;
		}

		HandyShulkerConfig config = HandyShulkerConfig.get();
		ItemStack targetStack = slot.getItem();

		if (action == ClickAction.PRIMARY && !targetStack.isEmpty()) {
			if (!config.enableClickInsert) return;
			if (HandyContainers.canInsert(containerStack, targetStack)) {
				int inserted = HandyContainers.tryInsert(containerStack, player, targetStack);
				if (inserted > 0) {
					playInsertSound(player);
				} else {
					playInsertFailSound(player);
				}
				cir.setReturnValue(true);
			}
		} else if (action == ClickAction.SECONDARY && targetStack.isEmpty()) {
			if (!config.enableScrollExtract) return;
			int extractIndex = handyshulker$getExtractIndex(player, slot.index, containerStack);
			ItemStack extracted = HandyContainers.removeOneStack(containerStack, player, extractIndex);
			if (!extracted.isEmpty()) {
				ItemStack remainder = slot.safeInsert(extracted);
				if (!remainder.isEmpty()) {
					HandyContainers.tryInsert(containerStack, player, remainder);
				} else {
					playRemoveSound(player);
				}
				handyshulker$clearSelection(player, slot.index);
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
	@Inject(method = "overrideOtherStackedOnMe(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/inventory/ClickAction;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/SlotAccess;)Z", at = @At("HEAD"), cancellable = true)
	private void handyshulker$onOtherStackedOnMe(
			ItemStack containerStack, ItemStack incomingStack, Slot slot, ClickAction action,
			Player player, SlotAccess slotAccess,
			CallbackInfoReturnable<Boolean> cir) {

		if (!HandyContainers.isActionAllowed(containerStack, player) || containerStack.getCount() != 1) {
			return;
		}

		HandyShulkerConfig config = HandyShulkerConfig.get();

		if (action == ClickAction.PRIMARY && !incomingStack.isEmpty()) {
			if (!config.enableClickInsert) return;
			if (HandyContainers.canInsert(containerStack, incomingStack)) {
				int inserted = HandyContainers.tryInsert(containerStack, player, incomingStack);
				if (inserted > 0) {
					playInsertSound(player);
				} else {
					playInsertFailSound(player);
				}
				cir.setReturnValue(true);
			}
		} else if (action == ClickAction.SECONDARY && incomingStack.isEmpty()) {
			if (!config.enableScrollExtract) return;
			if (slot.allowModification(player)) {
				int extractIndex = handyshulker$getExtractIndex(player, slot.index, containerStack);
				ItemStack extracted = HandyContainers.removeOneStack(containerStack, player, extractIndex);
				if (!extracted.isEmpty()) {
					playRemoveSound(player);
					slotAccess.set(extracted);
					handyshulker$clearSelection(player, slot.index);
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
	private static int handyshulker$getExtractIndex(Player player, int slotIndex, ItemStack containerStack) {
		List<ItemStack> contents = HandyContainers.getContents(containerStack, player);
		// If the player's container menu wasn't constructed through ContainerMenuMixin
		// (rare classloader edge case where another mod's AbstractContainerMenu subclass
		// loads before our mixin applies), there's no scroll-selection state to read —
		// fall back to the first non-empty slot, which matches the "no selection yet" path.
		if (!(player.containerMenu instanceof ShulkerSelectionManager manager)) {
			return handyshulker$firstNonEmptyIndex(contents);
		}
		int selectedNonEmptyIndex = manager.handyshulker$getSelection(slotIndex);
		if (selectedNonEmptyIndex < 0) {
			return handyshulker$firstNonEmptyIndex(contents);
		}

		int nonEmptyCount = 0;
		for (int i = 0; i < contents.size(); i++) {
			if (!contents.get(i).isEmpty()) {
				if (nonEmptyCount == selectedNonEmptyIndex) {
					return i;
				}
				nonEmptyCount++;
			}
		}
		return handyshulker$firstNonEmptyIndex(contents);
	}

	private static int handyshulker$firstNonEmptyIndex(List<ItemStack> contents) {
		for (int i = 0; i < contents.size(); i++) {
			if (!contents.get(i).isEmpty()) return i;
		}
		return 0;
	}

	/**
	 * Clear the scroll-selection for a slot after a successful extract. Without
	 * this, rapid right-clicks would re-interpret the same non-empty index
	 * against shifted contents and pull "the next" item, which feels wrong
	 * when positions are preserved (as they are for both shulkers and ender
	 * chests). The user can scroll again to re-target.
	 */
	private static void handyshulker$clearSelection(Player player, int slotIndex) {
		if (player.containerMenu instanceof ShulkerSelectionManager manager) {
			manager.handyshulker$clearSelection(slotIndex);
		}
	}

	// -- Fullness bar (matches bundle style) --

	private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);
	private static final int BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);
	/** Vanilla item-bar fixed pixel width — see {@code Item#getBarWidth} contract. */
	private static final int BAR_PIXEL_WIDTH = 13;
	/** Maximum span of the filled portion. The full {@link #BAR_PIXEL_WIDTH} is reserved
	 *  for the gutter on either side, so the fill caps at one less than the bar width. */
	private static final int BAR_FILL_MAX = BAR_PIXEL_WIDTH - 1;

	@Inject(method = "isBarVisible(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
	private void handyshulker$isBarVisible(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (HandyShulkerConfig.get().showFullnessBar && ShulkerBoxHelper.isShulkerBox(stack)) {
			cir.setReturnValue(ShulkerBoxHelper.getOccupiedSlots(stack) > 0);
		}
	}

	@Inject(method = "getBarWidth(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
	private void handyshulker$getBarWidth(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (HandyShulkerConfig.get().showFullnessBar && ShulkerBoxHelper.isShulkerBox(stack)) {
			int occupied = ShulkerBoxHelper.getOccupiedSlots(stack);
			cir.setReturnValue(Math.min(1 + BAR_FILL_MAX * occupied / ShulkerBoxHelper.SHULKER_SLOTS, BAR_PIXEL_WIDTH));
		}
	}

	@Inject(method = "getBarColor(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
	private void handyshulker$getBarColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (HandyShulkerConfig.get().showFullnessBar && ShulkerBoxHelper.isShulkerBox(stack)) {
			int occupied = ShulkerBoxHelper.getOccupiedSlots(stack);
			cir.setReturnValue(occupied >= ShulkerBoxHelper.SHULKER_SLOTS ? FULL_BAR_COLOR : BAR_COLOR);
		}
	}

	// -- Sound effects --

	private static void playInsertSound(Player player) {
		HandyShulkerConfig config = HandyShulkerConfig.get();
		if (!config.enableSounds) return;
		player.playSound(
				net.minecraft.sounds.SoundEvents.SHULKER_OPEN,
				0.4F * config.soundVolume,
				0.8F + player.level().getRandom().nextFloat() * 0.4F
		);
	}

	private static void playInsertFailSound(Player player) {
		HandyShulkerConfig config = HandyShulkerConfig.get();
		if (!config.enableSounds) return;
		player.playSound(
				net.minecraft.sounds.SoundEvents.SHULKER_HURT_CLOSED,
				0.3F * config.soundVolume, 0.9F
		);
	}

	private static void playRemoveSound(Player player) {
		HandyShulkerConfig config = HandyShulkerConfig.get();
		if (!config.enableSounds) return;
		player.playSound(
				net.minecraft.sounds.SoundEvents.SHULKER_CLOSE,
				0.4F * config.soundVolume,
				0.8F + player.level().getRandom().nextFloat() * 0.4F
		);
	}
}
