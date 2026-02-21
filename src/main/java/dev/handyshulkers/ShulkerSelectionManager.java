package dev.handyshulkers;

/**
 * Interface injected into AbstractContainerMenu via mixin.
 * Stores the selected item index for shulker boxes in inventory slots.
 *
 * This allows both the scroll handler (client) and the extraction mixin (shared)
 * to access the selection through player.containerMenu.
 */
public interface ShulkerSelectionManager {

	int handyshulkers$getSelection(int slotIndex);

	void handyshulkers$setSelection(int slotIndex, int selectedItemIndex);

	void handyshulkers$clearSelection(int slotIndex);
}
