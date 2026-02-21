package dev.handyshulkers;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Tooltip data for shulker boxes. Carries the item contents and selection state
 * to the client-side renderer (ClientShulkerTooltip) via Fabric's TooltipComponentCallback.
 *
 * @param items         All 27 slots (padded with EMPTY for empty slots)
 * @param occupiedSlots Number of non-empty slots
 * @param selectedIndex Index into the non-empty item list (-1 if none selected)
 */
public record ShulkerTooltip(List<ItemStack> items, int occupiedSlots, int selectedIndex) implements TooltipComponent {
}
