package dev.handy.mods.handyshulker;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for manipulating shulker box item inventories.
 *
 * Shulker boxes store their inventory as a CONTAINER data component (ItemContainerContents).
 * This helper provides methods to read, add, and remove items from shulker boxes
 * while they are still in item form (without placing/opening the block).
 */
public final class ShulkerBoxHelper {

	/** Shulker boxes have 27 slots (3 rows of 9) */
	public static final int SHULKER_SLOTS = 27;

	/** Maximum stack size for items inside a shulker box */
	public static final int MAX_STACK_SIZE = 64;

	private ShulkerBoxHelper() {}

	/**
	 * Check if the given ItemStack is a shulker box (any color, including undyed).
	 */
	public static boolean isShulkerBox(ItemStack stack) {
		// BlockItem's getBlock() returns the block associated with this item
		if (stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
			return blockItem.getBlock() instanceof ShulkerBoxBlock;
		}
		return false;
	}

	/**
	 * Get the contents of a shulker box as a mutable 27-slot list.
	 *
	 * Empty slots are preserved by position (ItemStack.EMPTY at that index) so
	 * slot positions survive insert/remove round-trips. Without this guarantee
	 * the internal CONTAINER list can grow past 27 and items at index ≥ 27 get
	 * silently dropped when the block entity calls copyInto() on place (#4).
	 */
	public static List<ItemStack> getContents(ItemStack shulkerStack) {
		List<ItemStack> items = new ArrayList<>(SHULKER_SLOTS);
		ItemContainerContents contents = shulkerStack.get(DataComponents.CONTAINER);
		if (contents != null) {
			contents.allItemsCopyStream().forEach(items::add);
		}
		while (items.size() < SHULKER_SLOTS) {
			items.add(ItemStack.EMPTY);
		}
		if (items.size() > SHULKER_SLOTS) {
			items.subList(SHULKER_SLOTS, items.size()).clear();
		}
		return items;
	}

	/**
	 * Set the contents of a shulker box from a list of ItemStacks.
	 */
	public static void setContents(ItemStack shulkerStack, List<ItemStack> items) {
		shulkerStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
	}

	/**
	 * Try to insert an ItemStack into a shulker box.
	 *
	 * @param shulkerStack The shulker box item stack
	 * @param toInsert     The item to insert
	 * @return The number of items actually inserted
	 */
	public static int tryInsert(ItemStack shulkerStack, ItemStack toInsert) {
		if (!canInsert(toInsert)) return 0;
		return SlotOps.tryInsert(forStack(shulkerStack), toInsert);
	}

	/**
	 * Remove and return one item from the shulker box at the given index.
	 * Returns ItemStack.EMPTY if no item at that index.
	 */
	public static ItemStack removeOneStack(ItemStack shulkerStack, int index) {
		return SlotOps.removeOne(forStack(shulkerStack), index);
	}

	/**
	 * Build a {@link SlotAccessor} backed by this shulker's CONTAINER component.
	 * The contents list is a fresh snapshot — in-place mutations on its stacks
	 * are local, and {@link SlotAccessor#commit} writes the snapshot back via
	 * {@link #setContents}.
	 */
	private static SlotAccessor forStack(ItemStack shulkerStack) {
		List<ItemStack> contents = getContents(shulkerStack);
		return new SlotAccessor() {
			@Override public int size() { return contents.size(); }
			@Override public ItemStack get(int i) { return contents.get(i); }
			@Override public void set(int i, ItemStack stack) { contents.set(i, stack); }
			@Override public void commit() { setContents(shulkerStack, contents); }
		};
	}

	/**
	 * Check if an item can be inserted into a shulker box.
	 * Shulker boxes cannot contain other shulker boxes.
	 */
	public static boolean canInsert(ItemStack stack) {
		return !isShulkerBox(stack);
	}

	/**
	 * Get the number of occupied slots in the shulker box.
	 */
	public static int getOccupiedSlots(ItemStack shulkerStack) {
		return (int) getContents(shulkerStack).stream().filter(s -> !s.isEmpty()).count();
	}

	/**
	 * Get the DyeColor of a shulker box, or null if undyed.
	 */
	public static DyeColor getColor(ItemStack stack) {
		if (stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem
				&& blockItem.getBlock() instanceof ShulkerBoxBlock shulkerBlock) {
			return shulkerBlock.getColor();
		}
		return null;
	}

	/**
	 * Check if the shulker box has any space left.
	 */
	public static boolean hasSpace(ItemStack shulkerStack) {
		List<ItemStack> contents = getContents(shulkerStack);
		int occupied = (int) contents.stream().filter(s -> !s.isEmpty()).count();
		if (occupied < SHULKER_SLOTS) return true;

		// Check if any existing stack can accept more
		// (not strictly needed for basic functionality, but nice)
		return false;
	}
}
