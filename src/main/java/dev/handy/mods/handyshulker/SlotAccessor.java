package dev.handy.mods.handyshulker;

import net.minecraft.world.item.ItemStack;

/**
 * Storage-agnostic view of a fixed-size slot collection. The two backing stores
 * we care about — a shulker box's CONTAINER component and a player's ender chest
 * inventory — have completely different commit semantics (rewrite the immutable
 * component vs mark a mutable container dirty) but identical
 * read/write-by-index access patterns. Hide the difference behind this interface
 * so {@link SlotOps} can run the merge-then-fill insert and remove algorithms
 * once for both.
 *
 * <p>Stacks returned from {@link #get} may be the live backing stack (ender
 * chest) or a copy (shulker box snapshot). Either way, in-place mutations such
 * as {@code stack.grow(...)} are valid as long as {@link #commit} is called
 * before the next read by an outside caller — that's how both backing stores
 * already worked individually.
 */
interface SlotAccessor {
	int size();
	ItemStack get(int i);
	void set(int i, ItemStack stack);
	/** Persist any mutations made via {@link #set} or in-place grows on stacks
	 *  returned from {@link #get}. Called once after all mutations are done. */
	void commit();
}
