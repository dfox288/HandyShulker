package dev.handy.mods.handyshulker;

import net.minecraft.world.item.ItemStack;

/**
 * Shared algorithms over any {@link SlotAccessor}. Used by both
 * {@link ShulkerBoxHelper} and {@link EnderChestHelper} so they don't drift
 * apart on the same merge-then-fill insert and remove logic.
 */
final class SlotOps {

	private SlotOps() {}

	/**
	 * Insert as much of {@code toInsert} as the slot collection will accept.
	 * Mutates {@code toInsert} by shrinking its count by however many were
	 * accepted, mirroring vanilla bundle behavior. Two-phase fill:
	 * <ol>
	 *   <li><b>Merge</b> — top up matching non-empty slots up to their max stack size.</li>
	 *   <li><b>Fill</b> — drop the remainder into empty slots in order.</li>
	 * </ol>
	 * Calls {@link SlotAccessor#commit} exactly once at the end if anything was
	 * inserted, so a no-op insert is also a no-op write.
	 */
	static int tryInsert(SlotAccessor slots, ItemStack toInsert) {
		if (toInsert.isEmpty()) return 0;
		int size = slots.size();
		int inserted = 0;

		// Merge phase — top up matching stacks at their existing positions.
		for (int i = 0; i < size && inserted < toInsert.getCount(); i++) {
			ItemStack existing = slots.get(i);
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

		// Fill phase — drop the remainder into empty slots by index.
		for (int i = 0; i < size && inserted < toInsert.getCount(); i++) {
			if (slots.get(i).isEmpty()) {
				int toAdd = Math.min(toInsert.getMaxStackSize(), toInsert.getCount() - inserted);
				slots.set(i, toInsert.copyWithCount(toAdd));
				inserted += toAdd;
			}
		}

		if (inserted > 0) {
			slots.commit();
			toInsert.shrink(inserted);
		}
		return inserted;
	}

	/**
	 * Remove and return the stack at {@code index}, replacing it with EMPTY.
	 * Returns EMPTY if the index is out of range or the slot is already empty
	 * (no commit fires in either case).
	 */
	static ItemStack removeOne(SlotAccessor slots, int index) {
		if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
		ItemStack removed = slots.get(index);
		if (removed.isEmpty()) return ItemStack.EMPTY;
		slots.set(index, ItemStack.EMPTY);
		slots.commit();
		return removed;
	}
}
