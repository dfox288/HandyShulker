package dev.handyshulkers.client;

import dev.handyshulkers.config.HandyShulkersConfig;
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
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders a shulker box tooltip in two modes:
 * - Grid mode (default): 9x3 slot grid matching the shulker inventory layout
 * - Compact mode (hold Shift): packed grid of unique items with aggregated counts, no empty slots
 *
 * The tooltip border is tinted to match the shulker box color.
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
	private static final int BORDER = 2;

	/** Default border color for undyed shulker boxes (vanilla shulker purple) */
	private static final int DEFAULT_BORDER_COLOR = ARGB.colorFromFloat(0.6F, 0.59F, 0.42F, 0.66F);

	private final List<ItemStack> items;
	private final int selectedIndex; // Index into non-empty items, -1 if none
	private final DyeColor color; // null for undyed
	private final List<UniqueItem> uniqueItems;

	public ClientShulkerTooltip(List<ItemStack> items, int occupiedSlots, int selectedIndex,
								DyeColor color) {
		this.items = items;
		this.selectedIndex = selectedIndex;
		this.color = color;
		this.uniqueItems = computeUniqueItems();
	}

	@Override
	public int getHeight(Font font) {
		if (isCompactMode() && !uniqueItems.isEmpty()) {
			int cols = getCompactColumns();
			int rows = (uniqueItems.size() + cols - 1) / cols;
			return rows * SLOT_SIZE + BORDER * 2;
		}
		return ROWS * SLOT_SIZE + BORDER * 2;
	}

	@Override
	public int getWidth(Font font) {
		if (isCompactMode() && !uniqueItems.isEmpty()) {
			int cols = getCompactColumns();
			return cols * SLOT_SIZE + BORDER * 2;
		}
		return GRID_WIDTH + BORDER * 2;
	}

	@Override
	public boolean showTooltipWithItemInHand() {
		return true;
	}

	@Override
	public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics guiGraphics) {
		if (isCompactMode() && !uniqueItems.isEmpty()) {
			renderCompact(font, x, y, width, guiGraphics);
		} else {
			renderGrid(font, x, y, width, guiGraphics);
		}
	}

	// -- Grid mode rendering (default) --

	private void renderGrid(Font font, int x, int y, int width, GuiGraphics guiGraphics) {
		int offsetX = (width - GRID_WIDTH - BORDER * 2) / 2;
		int borderX = x + offsetX;
		int borderY = y;
		int gridX = borderX + BORDER;
		int gridY = borderY + BORDER;
		int selectedGridIndex = getSelectedGridIndex();

		drawBorder(guiGraphics, borderX, borderY, GRID_WIDTH + BORDER * 2, ROWS * SLOT_SIZE + BORDER * 2);

		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				int slotX = gridX + col * SLOT_SIZE;
				int slotY = gridY + row * SLOT_SIZE;
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

		drawSelectedItemName(font, guiGraphics, x, y, width);
	}

	// -- Compact mode rendering (Shift held) --

	private void renderCompact(Font font, int x, int y, int width, GuiGraphics guiGraphics) {
		int cols = getCompactColumns();
		int rows = (uniqueItems.size() + cols - 1) / cols;
		int totalWidth = cols * SLOT_SIZE + BORDER * 2;
		int totalHeight = rows * SLOT_SIZE + BORDER * 2;
		int offsetX = (width - totalWidth) / 2;
		int borderX = x + offsetX;
		int borderY = y;
		int gridX = borderX + BORDER;
		int gridY = borderY + BORDER;

		drawBorder(guiGraphics, borderX, borderY, totalWidth, totalHeight);

		// Determine selected item for highlighting
		ItemStack selectedStack = getSelectedStack();

		for (int i = 0; i < uniqueItems.size(); i++) {
			UniqueItem item = uniqueItems.get(i);
			int col = i % cols;
			int row = i / cols;
			int slotX = gridX + col * SLOT_SIZE;
			int slotY = gridY + row * SLOT_SIZE;

			boolean isSelected = selectedStack != null
					&& ItemStack.isSameItemSameComponents(item.stack, selectedStack);

			if (isSelected) {
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
			} else {
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_BACKGROUND_SPRITE, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
			}

			// Render item with abbreviated count label
			guiGraphics.renderItem(item.stack, slotX + 4, slotY + 4);
			String countLabel = HandyShulkersConfig.get().showItemCounts ? formatCount(item.totalCount) : "";
			guiGraphics.renderItemDecorations(font, item.stack, slotX + 4, slotY + 4, countLabel);

			if (isSelected) {
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
			}
		}
	}

	/**
	 * Number of columns for compact mode — adapts to item count.
	 */
	private int getCompactColumns() {
		int count = uniqueItems.size();
		if (count <= 4) return count;
		if (count <= 8) return (count + 1) / 2;
		return Math.min(count, 9);
	}

	// -- Shared rendering helpers --

	private void drawBorder(GuiGraphics guiGraphics, int x, int y, int w, int h) {
		if (!HandyShulkersConfig.get().showColoredBorders) return;
		int borderColor = getBorderColor();
		guiGraphics.fill(x, y, x + w, y + BORDER, borderColor);
		guiGraphics.fill(x, y + h - BORDER, x + w, y + h, borderColor);
		guiGraphics.fill(x, y + BORDER, x + BORDER, y + h - BORDER, borderColor);
		guiGraphics.fill(x + w - BORDER, y + BORDER, x + w, y + h - BORDER, borderColor);
	}

	private int getBorderColor() {
		if (color == null) {
			return DEFAULT_BORDER_COLOR;
		}
		int rgb = color.getTextureDiffuseColor();
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		return ARGB.colorFromFloat(0.8F, r / 255.0F, g / 255.0F, b / 255.0F);
	}

	private void drawSelectedItemName(Font font, GuiGraphics guiGraphics, int x, int y, int width) {
		if (!HandyShulkersConfig.get().showItemName) return;
		int selectedGridIndex = getSelectedGridIndex();
		if (selectedGridIndex < 0 || selectedGridIndex >= items.size()) return;

		ItemStack selectedStack = items.get(selectedGridIndex);
		if (selectedStack.isEmpty()) return;

		int totalCount = getTotalCount(selectedStack);

		Component name;
		if (totalCount > selectedStack.getCount()) {
			name = Component.empty()
					.append(selectedStack.getStyledHoverName())
					.append(Component.literal(" x" + totalCount).withColor(0xAAAAAA));
		} else {
			name = selectedStack.getStyledHoverName();
		}

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
	 * Format item count for display, abbreviating large numbers.
	 */
	private static String formatCount(int count) {
		if (count <= 1) return "";
		if (count < 1000) return String.valueOf(count);
		if (count < 10000) return String.format("%.1fk", count / 1000.0);
		return (count / 1000) + "k";
	}

	// -- Data helpers --

	private static boolean isCompactMode() {
		return ShulkerClientUtil.isCompactMode();
	}

	private ItemStack getSelectedStack() {
		int gridIndex = getSelectedGridIndex();
		if (gridIndex < 0 || gridIndex >= items.size()) return null;
		ItemStack stack = items.get(gridIndex);
		return stack.isEmpty() ? null : stack;
	}

	private int getTotalCount(ItemStack target) {
		int total = 0;
		for (ItemStack stack : items) {
			if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, target)) {
				total += stack.getCount();
			}
		}
		return total;
	}

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

	private List<UniqueItem> computeUniqueItems() {
		List<UniqueItem> result = new ArrayList<>();
		for (ItemStack stack : items) {
			if (stack.isEmpty()) continue;
			boolean found = false;
			for (UniqueItem existing : result) {
				if (ItemStack.isSameItemSameComponents(existing.stack, stack)) {
					found = true;
					break;
				}
			}
			if (!found) {
				result.add(new UniqueItem(stack, getTotalCount(stack)));
			}
		}
		return result;
	}

	private record UniqueItem(ItemStack stack, int totalCount) {}
}
