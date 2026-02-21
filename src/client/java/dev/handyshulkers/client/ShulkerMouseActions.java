package dev.handyshulkers.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.handyshulkers.ShulkerBoxHelper;
import dev.handyshulkers.ShulkerSelectionManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

import java.util.List;

/**
 * Handles mouse scroll events over shulker box slots in the inventory.
 * Scrolling cycles through the shulker's items, selecting one for extraction.
 *
 * Reuses the ServerboundSelectBundleItemPacket to sync the selection with the server.
 */
@Environment(EnvType.CLIENT)
public class ShulkerMouseActions implements ItemSlotMouseAction {

	/** Tracks the slot index currently being hovered (for tooltip rendering) */
	public static int lastHoveredSlotIndex = -1;

	/** Active instance for use by the AllowMouseScroll event callback */
	private static ShulkerMouseActions activeInstance;

	private final Minecraft minecraft;
	private final ScrollWheelHandler scrollWheelHandler;

	public ShulkerMouseActions(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.scrollWheelHandler = new ScrollWheelHandler();
		activeInstance = this;
	}

	/**
	 * Called from the Fabric AllowMouseScroll event to handle shulker scrolling
	 * before Mouse Tweaks (or other mods) can intercept it.
	 * Returns true if the scroll was consumed (caller should block the event).
	 */
	public static boolean handleScrollEvent(double scrollX, double scrollY) {
		if (activeInstance == null || lastHoveredSlotIndex < 0) return false;

		Minecraft mc = activeInstance.minecraft;
		if (mc.player == null || mc.player.containerMenu == null) return false;

		int slotIndex = lastHoveredSlotIndex;
		if (slotIndex >= mc.player.containerMenu.slots.size()) return false;

		Slot slot = mc.player.containerMenu.slots.get(slotIndex);
		ItemStack stack = slot.getItem();
		if (!ShulkerBoxHelper.isShulkerBox(stack)) return false;

		return activeInstance.onMouseScrolled(scrollX, scrollY, slotIndex, stack);
	}

	@Override
	public boolean matches(Slot slot) {
		boolean isShulker = ShulkerBoxHelper.isShulkerBox(slot.getItem());
		if (isShulker) {
			lastHoveredSlotIndex = slot.index;
		}
		return isShulker;
	}

	@Override
	public boolean onMouseScrolled(double scrollX, double scrollY, int slotIndex, ItemStack itemStack) {
		// In compact mode (Shift held), consume scroll but don't change selection
		if (isShiftHeld()) {
			return true;
		}

		List<ItemStack> contents = ShulkerBoxHelper.getContents(itemStack);
		// Only count non-empty items for scrolling
		int itemCount = (int) contents.stream().filter(s -> !s.isEmpty()).count();
		if (itemCount == 0) {
			return false;
		}

		Vector2i scroll = this.scrollWheelHandler.onMouseScroll(scrollX, scrollY);
		int delta = scroll.y == 0 ? -scroll.x : scroll.y;
		if (delta != 0) {
			ShulkerSelectionManager selectionManager = (ShulkerSelectionManager) this.minecraft.player.containerMenu;
			int currentSelection = selectionManager.handyshulkers$getSelection(slotIndex);
			int newSelection = ScrollWheelHandler.getNextScrollWheelSelection(delta, currentSelection, itemCount);

			if (currentSelection != newSelection) {
				setSelection(itemStack, slotIndex, newSelection);
			}
		}

		return true;
	}

	private static boolean isShiftHeld() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT)
				|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

	@Override
	public void onStopHovering(Slot slot) {
		lastHoveredSlotIndex = -1;
		clearSelection(slot.getItem(), slot.index);
	}

	@Override
	public void onSlotClicked(Slot slot, ClickType clickType) {
		if (clickType == ClickType.QUICK_MOVE || clickType == ClickType.SWAP) {
			clearSelection(slot.getItem(), slot.index);
		}
	}

	private void setSelection(ItemStack itemStack, int slotIndex, int selectedIndex) {
		ClientPacketListener connection = this.minecraft.getConnection();
		if (connection == null) return;

		// Update client-side selection
		ShulkerSelectionManager selectionManager = (ShulkerSelectionManager) this.minecraft.player.containerMenu;
		selectionManager.handyshulkers$setSelection(slotIndex, selectedIndex);

		// Sync to server by reusing the bundle selection packet
		connection.send(new ServerboundSelectBundleItemPacket(slotIndex, selectedIndex));
	}

	private void clearSelection(ItemStack itemStack, int slotIndex) {
		setSelection(itemStack, slotIndex, -1);
	}
}
