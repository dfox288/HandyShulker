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
		if (!canInsert(toInsert)) return 0;
		return SlotOps.tryInsert(forPlayer(player), toInsert);
	}

	/**
	 * Remove and return the stack at {@code index} (replaces with EMPTY).
	 */
	public static ItemStack removeOneStack(Player player, int index) {
		return SlotOps.removeOne(forPlayer(player), index);
	}

	/**
	 * Build a {@link SlotAccessor} backed by the player's ender chest inventory.
	 * Stacks returned by {@code get} are the live container stacks, so in-place
	 * grows during the merge phase are persisted directly; {@code commit} just
	 * marks the container dirty.
	 */
	private static SlotAccessor forPlayer(Player player) {
		PlayerEnderChestContainer inv = player.getEnderChestInventory();
		return new SlotAccessor() {
			@Override public int size() { return ENDER_CHEST_SLOTS; }
			@Override public ItemStack get(int i) { return inv.getItem(i); }
			@Override public void set(int i, ItemStack stack) { inv.setItem(i, stack); }
			@Override public void commit() { inv.setChanged(); }
		};
	}

	/**
	 * Ender chests have no insert restrictions. Shulker boxes get the
	 * "no nesting" rule because each shulker carries its own contents on the
	 * item, so a shulker-in-shulker would be a recursive container. Ender
	 * chest items don't carry contents — they all share the player's
	 * single ender storage when placed — so dropping ender chest items
	 * into the ender chest inventory is just storing items in a 27-slot
	 * box, no recursion.
	 */
	public static boolean canInsert(ItemStack stack) {
		return true;
	}
}
