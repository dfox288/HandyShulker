package dev.handy.mods.handyshulker;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EnderChestBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for manipulating a player's ender chest inventory.
 *
 * Ender chests don't carry a CONTAINER component on the item — their contents
 * live on {@link Player#getEnderChestInventory()} (27 slots, server-authoritative).
 * All mutating operations run server-side; the client mirrors state via a
 * custom sync payload (see {@code SyncEnderChestPayload}).
 */
public final class EnderChestHelper {

	public static final int ENDER_CHEST_SLOTS = 27;

	private EnderChestHelper() {}

	public static boolean isEnderChest(ItemStack stack) {
		if (stack.getItem() instanceof BlockItem blockItem) {
			return blockItem.getBlock() instanceof EnderChestBlock;
		}
		return false;
	}

	/**
	 * Snapshot the player's ender chest as a 27-slot list (copies of each stack).
	 */
	public static List<ItemStack> snapshot(Player player) {
		PlayerEnderChestContainer inv = player.getEnderChestInventory();
		List<ItemStack> items = new ArrayList<>(ENDER_CHEST_SLOTS);
		for (int i = 0; i < ENDER_CHEST_SLOTS; i++) {
			items.add(inv.getItem(i).copy());
		}
		return items;
	}

	public static int getOccupiedSlots(Player player) {
		PlayerEnderChestContainer inv = player.getEnderChestInventory();
		int n = 0;
		for (int i = 0; i < ENDER_CHEST_SLOTS; i++) {
			if (!inv.getItem(i).isEmpty()) n++;
		}
		return n;
	}

	/**
	 * Try to insert into the player's ender chest. Mutates {@code toInsert} by
	 * shrinking its count by however many were accepted. Returns inserted count.
	 */
	public static int tryInsert(Player player, ItemStack toInsert) {
		if (toInsert.isEmpty() || !canInsert(toInsert)) return 0;
		PlayerEnderChestContainer inv = player.getEnderChestInventory();
		int inserted = 0;

		// Merge into matching non-empty slots.
		for (int i = 0; i < ENDER_CHEST_SLOTS && inserted < toInsert.getCount(); i++) {
			ItemStack existing = inv.getItem(i);
			if (existing.isEmpty()) continue;
			if (ItemStack.isSameItemSameComponents(existing, toInsert)) {
				int space = existing.getMaxStackSize() - existing.getCount();
				if (space > 0) {
					int toAdd = Math.min(space, toInsert.getCount() - inserted);
					existing.grow(toAdd);
					inserted += toAdd;
				}
			}
		}

		// Fill empty slots by index.
		for (int i = 0; i < ENDER_CHEST_SLOTS && inserted < toInsert.getCount(); i++) {
			if (inv.getItem(i).isEmpty()) {
				int toAdd = Math.min(toInsert.getMaxStackSize(), toInsert.getCount() - inserted);
				inv.setItem(i, toInsert.copyWithCount(toAdd));
				inserted += toAdd;
			}
		}

		if (inserted > 0) {
			inv.setChanged();
			toInsert.shrink(inserted);
		}
		return inserted;
	}

	/**
	 * Remove and return the stack at {@code index} (replaces with EMPTY).
	 */
	public static ItemStack removeOneStack(Player player, int index) {
		if (index < 0 || index >= ENDER_CHEST_SLOTS) return ItemStack.EMPTY;
		PlayerEnderChestContainer inv = player.getEnderChestInventory();
		ItemStack removed = inv.getItem(index);
		if (removed.isEmpty()) return ItemStack.EMPTY;
		inv.setItem(index, ItemStack.EMPTY);
		inv.setChanged();
		return removed;
	}

	/** Disallow nested ender chests (vanilla rule). Shulker boxes are permitted. */
	public static boolean canInsert(ItemStack stack) {
		return !isEnderChest(stack);
	}
}
