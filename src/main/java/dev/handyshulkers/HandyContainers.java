package dev.handyshulkers;

import dev.handyshulkers.config.HandyShulkersConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Dispatcher that routes insert/extract/query operations to either
 * {@link ShulkerBoxHelper} (shulker boxes) or {@link EnderChestHelper}
 * (ender chests). Callers can treat both container types uniformly.
 *
 * Each method consults {@link HandyShulkersConfig} to respect the global
 * feature toggles, so disabling ender chest support at runtime immediately
 * stops dispatching to the ender chest path.
 */
public final class HandyContainers {

	private HandyContainers() {}

	/** True if this item is a supported container (shulker or ender chest). */
	public static boolean isSupported(ItemStack stack) {
		if (ShulkerBoxHelper.isShulkerBox(stack)) return true;
		if (EnderChestHelper.isEnderChest(stack) && HandyShulkersConfig.get().enableEnderChestSupport) return true;
		return false;
	}

	/**
	 * True if the player may currently act on this container via our mod.
	 *
	 * Creative inventory clicks route through {@code CreativeModeInventoryScreen}
	 * and bypass the server-side click handler, so our server mixin never fires
	 * for ender chests in creative — which caused duplication. Shulkers keep
	 * their state on the ItemStack itself so creative slot packets carry the
	 * mutation correctly; only ender chest needs the guard.
	 */
	public static boolean isActionAllowed(ItemStack stack, Player player) {
		if (!isSupported(stack)) return false;
		if (EnderChestHelper.isEnderChest(stack) && player != null && player.getAbilities().instabuild) {
			return false;
		}
		return true;
	}

	/** 27-slot position-preserved contents list, or empty list if unsupported. */
	public static List<ItemStack> getContents(ItemStack stack, Player player) {
		if (ShulkerBoxHelper.isShulkerBox(stack)) {
			return ShulkerBoxHelper.getContents(stack);
		}
		if (EnderChestHelper.isEnderChest(stack) && HandyShulkersConfig.get().enableEnderChestSupport && player != null) {
			return EnderChestHelper.snapshot(player);
		}
		return List.of();
	}

	public static int getOccupiedSlots(ItemStack stack, Player player) {
		int n = 0;
		for (ItemStack s : getContents(stack, player)) {
			if (!s.isEmpty()) n++;
		}
		return n;
	}

	public static boolean canInsert(ItemStack container, ItemStack toInsert) {
		if (ShulkerBoxHelper.isShulkerBox(container)) return ShulkerBoxHelper.canInsert(toInsert);
		if (EnderChestHelper.isEnderChest(container)) return EnderChestHelper.canInsert(toInsert);
		return false;
	}

	/** Returns items inserted. Mutates {@code toInsert} by shrinking the accepted count. */
	public static int tryInsert(ItemStack container, Player player, ItemStack toInsert) {
		if (ShulkerBoxHelper.isShulkerBox(container)) {
			return ShulkerBoxHelper.tryInsert(container, toInsert);
		}
		if (EnderChestHelper.isEnderChest(container) && HandyShulkersConfig.get().enableEnderChestSupport && player != null) {
			int n = EnderChestHelper.tryInsert(player, toInsert);
			if (n > 0) syncIfServer(player);
			return n;
		}
		return 0;
	}

	public static ItemStack removeOneStack(ItemStack container, Player player, int index) {
		if (ShulkerBoxHelper.isShulkerBox(container)) {
			return ShulkerBoxHelper.removeOneStack(container, index);
		}
		if (EnderChestHelper.isEnderChest(container) && HandyShulkersConfig.get().enableEnderChestSupport && player != null) {
			ItemStack removed = EnderChestHelper.removeOneStack(player, index);
			if (!removed.isEmpty()) syncIfServer(player);
			return removed;
		}
		return ItemStack.EMPTY;
	}

	/** Dye color for the container, or null for undyed/ender chest. */
	public static DyeColor getColor(ItemStack stack) {
		if (ShulkerBoxHelper.isShulkerBox(stack)) return ShulkerBoxHelper.getColor(stack);
		return null;
	}

	private static void syncIfServer(Player player) {
		if (player instanceof ServerPlayer sp) {
			HandyShulkers.syncEnderChest(sp);
		}
	}
}
