package dev.handyshulkers;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
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
	 * Get the contents of a shulker box as a mutable list.
	 * Returns empty list if the shulker has no contents.
	 */
	public static List<ItemStack> getContents(ItemStack shulkerStack) {
		ItemContainerContents contents = shulkerStack.get(DataComponents.CONTAINER);
		if (contents == null) {
			return new ArrayList<>();
		}

		List<ItemStack> items = new ArrayList<>();
		contents.stream().forEach(items::add);
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
		if (toInsert.isEmpty() || !canInsert(toInsert)) {
			return 0;
		}

		List<ItemStack> contents = getContents(shulkerStack);
		int inserted = 0;

		// First, try to merge with existing stacks
		for (ItemStack existing : contents) {
			if (inserted >= toInsert.getCount()) break;
			if (ItemStack.isSameItemSameComponents(existing, toInsert)) {
				int space = existing.getMaxStackSize() - existing.getCount();
				if (space > 0) {
					int toAdd = Math.min(space, toInsert.getCount() - inserted);
					existing.grow(toAdd);
					inserted += toAdd;
				}
			}
		}

		// Then, try to add to empty slots
		if (inserted < toInsert.getCount()) {
			int occupiedSlots = (int) contents.stream().filter(s -> !s.isEmpty()).count();
			while (inserted < toInsert.getCount() && occupiedSlots < SHULKER_SLOTS) {
				int toAdd = Math.min(toInsert.getMaxStackSize(), toInsert.getCount() - inserted);
				ItemStack newStack = toInsert.copyWithCount(toAdd);
				contents.add(newStack);
				inserted += toAdd;
				occupiedSlots++;
			}
		}

		if (inserted > 0) {
			setContents(shulkerStack, contents);
			toInsert.shrink(inserted);
		}

		return inserted;
	}

	/**
	 * Remove and return one item from the shulker box at the given index.
	 * Returns ItemStack.EMPTY if no item at that index.
	 */
	public static ItemStack removeOneStack(ItemStack shulkerStack, int index) {
		List<ItemStack> contents = getContents(shulkerStack);
		if (index < 0 || index >= contents.size()) {
			return ItemStack.EMPTY;
		}

		ItemStack removed = contents.remove(index);
		setContents(shulkerStack, contents);
		return removed;
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
