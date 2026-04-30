package dev.handy.mods.handyshulker;

/**
 * Interface injected into AbstractContainerMenu via mixin.
 * Stores the selected item index for shulker boxes in inventory slots.
 *
 * This allows both the scroll handler (client) and the extraction mixin (shared)
 * to access the selection through player.containerMenu.
 */
public interface ShulkerSelectionManager {

	int handyshulker$getSelection(int slotIndex);

	void handyshulker$setSelection(int slotIndex, int selectedItemIndex);

	void handyshulker$clearSelection(int slotIndex);
}
