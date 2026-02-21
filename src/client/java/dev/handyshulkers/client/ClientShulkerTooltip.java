package dev.handyshulkers.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Renders a shulker box tooltip as a 9x3 grid (matching the actual shulker inventory layout).
 * Empty slots are shown as background sprites, occupied slots show their items.
 * The selected item (from scrolling) is highlighted and its name shown above the tooltip.
 */
@Environment(EnvType.CLIENT)
public class ClientShulkerTooltip implements ClientTooltipComponent {

	private static final Identifier SLOT_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_background");
	private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_back");
	private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_front");

	private static final int COLUMNS = 9;
	private static final int ROWS = 3;
	private static final int SLOT_SIZE = 24;
	private static final int GRID_WIDTH = COLUMNS * SLOT_SIZE;

	private final List<ItemStack> items;
	private final int selectedIndex; // Index into non-empty items, -1 if none

	public ClientShulkerTooltip(List<ItemStack> items, int occupiedSlots, int selectedIndex) {
		this.items = items;
		this.selectedIndex = selectedIndex;
	}

	@Override
	public int getHeight(Font font) {
		return ROWS * SLOT_SIZE;
	}

	@Override
	public int getWidth(Font font) {
		return GRID_WIDTH;
	}

	@Override
	public boolean showTooltipWithItemInHand() {
		return true;
	}

	@Override
	public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics guiGraphics) {
		int offsetX = (width - GRID_WIDTH) / 2;
		int selectedGridIndex = getSelectedGridIndex();

		// Draw the 9x3 grid
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				int slotX = x + offsetX + col * SLOT_SIZE;
				int slotY = y + row * SLOT_SIZE;
				int index = row * COLUMNS + col;
				boolean isSelected = (index == selectedGridIndex);

				if (isSelected) {
					guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
				} else {
					guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_BACKGROUND_SPRITE, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
				}

				if (index < items.size() && !items.get(index).isEmpty()) {
					ItemStack stack = items.get(index);
					guiGraphics.renderItem(stack, slotX + 4, slotY + 4);
					guiGraphics.renderItemDecorations(font, stack, slotX + 4, slotY + 4);
				}

				if (isSelected) {
					guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
				}
			}
		}

		// Show selected item name above the tooltip (like bundles do)
		drawSelectedItemName(font, guiGraphics, x, y, width);
	}

	/**
	 * Renders the selected item's name in a mini tooltip above the grid,
	 * matching how vanilla bundles display the hovered item name.
	 */
	private void drawSelectedItemName(Font font, GuiGraphics guiGraphics, int x, int y, int width) {
		int selectedGridIndex = getSelectedGridIndex();
		if (selectedGridIndex < 0 || selectedGridIndex >= items.size()) return;

		ItemStack selectedStack = items.get(selectedGridIndex);
		if (selectedStack.isEmpty()) return;

		Component name = selectedStack.getStyledHoverName();
		int nameWidth = font.width(name.getVisualOrderText());
		int centerX = x + width / 2 - 12;
		ClientTooltipComponent nameComponent = ClientTooltipComponent.create(name.getVisualOrderText());
		guiGraphics.renderTooltip(
				font,
				List.of(nameComponent),
				centerX - nameWidth / 2,
				y - 15,
				DefaultTooltipPositioner.INSTANCE,
				selectedStack.get(DataComponents.TOOLTIP_STYLE)
		);
	}

	/**
	 * Convert the selected non-empty item index to the actual 27-slot grid index.
	 */
	private int getSelectedGridIndex() {
		if (selectedIndex < 0) return -1;

		int nonEmptyCount = 0;
		for (int i = 0; i < items.size(); i++) {
			if (!items.get(i).isEmpty()) {
				if (nonEmptyCount == selectedIndex) {
					return i;
				}
				nonEmptyCount++;
			}
		}
		return -1;
	}
}
