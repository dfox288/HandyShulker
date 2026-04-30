package dev.handy.mods.handyshulker.client;

import dev.handy.mods.handyshulker.config.HandyShulkerConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders a shulker box tooltip in two modes:
 * - Grid mode (default): 9xN slot grid matching the shulker inventory layout (rows auto-collapse)
 * - Compact mode (hold Shift): packed grid of unique items with aggregated counts, no empty slots
 *
 * The tooltip border is tinted to match the shulker box color.
 */
public class ClientShulkerTooltip implements ClientTooltipComponent {

	private static final Identifier SLOT_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_background");
	private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_back");
	private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_front");

	private static final int COLUMNS = 9;
	private static final int BORDER = 2;

	/** Default border color for undyed shulker boxes (vanilla shulker purple) */
	private static final int DEFAULT_BORDER_COLOR = ARGB.colorFromFloat(0.6F, 0.59F, 0.42F, 0.66F);

	private final List<ItemStack> items;
	private final int selectedIndex; // Index into non-empty items, -1 if none
	private final DyeColor color; // null for undyed
	private final boolean isEnderChest;
	private final List<UniqueItem> uniqueItems;

	public ClientShulkerTooltip(List<ItemStack> items, int occupiedSlots, int selectedIndex,
								DyeColor color, boolean isEnderChest) {
		this.items = items;
		this.selectedIndex = selectedIndex;
		this.color = color;
		this.isEnderChest = isEnderChest;
		this.uniqueItems = computeUniqueItems();
	}

	private static int slotSize() {
		return HandyShulkerConfig.get().tooltipSize.slotSize;
	}

	/**
	 * Number of visible rows in grid mode.
	 *
	 * Auto-collapses to the last occupied row by default; returns the full 3 rows
	 * when {@code showAllSlots} is enabled, so users who prefer a stable layout
	 * always see the complete shulker inventory grid.
	 */
	private int visibleRows() {
		if (HandyShulkerConfig.get().showAllSlots) {
			return (items.size() + COLUMNS - 1) / COLUMNS;
		}
		int lastOccupiedRow = -1;
		for (int i = 0; i < items.size(); i++) {
			if (!items.get(i).isEmpty()) {
				lastOccupiedRow = i / COLUMNS;
			}
		}
		return lastOccupiedRow + 1;
	}

	@Override
	public int getHeight(Font font) {
		if (isCompactMode() && !uniqueItems.isEmpty()) {
			int ss = slotSize();
			int cols = getCompactColumns();
			int rows = (uniqueItems.size() + cols - 1) / cols;
			return rows * ss + BORDER * 2;
		}
		int rows = visibleRows();
		if (rows == 0) return 0;
		return rows * slotSize() + BORDER * 2;
	}

	@Override
	public int getWidth(Font font) {
		if (isCompactMode() && !uniqueItems.isEmpty()) {
			int cols = getCompactColumns();
			return cols * slotSize() + BORDER * 2;
		}
		int rows = visibleRows();
		if (rows == 0) return 0;
		return COLUMNS * slotSize() + BORDER * 2;
	}

	@Override
	public boolean showTooltipWithItemInHand() {
		return true;
	}

	@Override
	public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor guiGraphics) {
		if (isCompactMode() && !uniqueItems.isEmpty()) {
			renderCompact(font, x, y, width, guiGraphics);
		} else if (visibleRows() > 0) {
			renderGrid(font, x, y, width, guiGraphics);
		}
	}

	// -- Grid mode rendering (default) --

	private void renderGrid(Font font, int x, int y, int width, GuiGraphicsExtractor guiGraphics) {
		int rows = visibleRows();
		if (rows == 0) return;

		int ss = slotSize();
		int gridWidth = COLUMNS * ss;
		int itemOffset = (ss - 16) / 2;
		int offsetX = (width - gridWidth - BORDER * 2) / 2;
		int borderX = x + offsetX;
		int borderY = y;
		int gridX = borderX + BORDER;
		int gridY = borderY + BORDER;
		int selectedGridIndex = getSelectedGridIndex();

		drawBorder(guiGraphics, borderX, borderY, gridWidth + BORDER * 2, rows * ss + BORDER * 2);

		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				int slotX = gridX + col * ss;
				int slotY = gridY + row * ss;
				int index = row * COLUMNS + col;
				boolean isSelected = (index == selectedGridIndex);

				if (isSelected) {
					guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, slotX, slotY, ss, ss);
				} else {
					guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_BACKGROUND_SPRITE, slotX, slotY, ss, ss);
				}

				if (index < items.size() && !items.get(index).isEmpty()) {
					ItemStack stack = items.get(index);
					guiGraphics.item(stack, slotX + itemOffset, slotY + itemOffset);
					guiGraphics.itemDecorations(font, stack, slotX + itemOffset, slotY + itemOffset);
				}

				if (isSelected) {
					guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, slotX, slotY, ss, ss);
				}
			}
		}

		drawSelectedItemName(font, guiGraphics, x, y, width);
	}

	// -- Compact mode rendering (Shift held) --

	private void renderCompact(Font font, int x, int y, int width, GuiGraphicsExtractor guiGraphics) {
		int ss = slotSize();
		int itemOffset = (ss - 16) / 2;
		int cols = getCompactColumns();
		int rows = (uniqueItems.size() + cols - 1) / cols;
		int totalWidth = cols * ss + BORDER * 2;
		int totalHeight = rows * ss + BORDER * 2;
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
			int slotX = gridX + col * ss;
			int slotY = gridY + row * ss;

			boolean isSelected = selectedStack != null
					&& ItemStack.isSameItemSameComponents(item.stack, selectedStack);

			if (isSelected) {
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, slotX, slotY, ss, ss);
			} else {
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_BACKGROUND_SPRITE, slotX, slotY, ss, ss);
			}

			// Render item with abbreviated count label
			guiGraphics.item(item.stack, slotX + itemOffset, slotY + itemOffset);
			String countLabel = HandyShulkerConfig.get().showItemCounts ? formatCount(item.totalCount) : "";
			guiGraphics.itemDecorations(font, item.stack, slotX + itemOffset, slotY + itemOffset, countLabel);

			if (isSelected) {
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, slotX, slotY, ss, ss);
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

	private void drawBorder(GuiGraphicsExtractor guiGraphics, int x, int y, int w, int h) {
		if (!HandyShulkerConfig.get().showColoredBorders) return;
		int borderColor = getBorderColor();
		guiGraphics.fill(x, y, x + w, y + BORDER, borderColor);
		guiGraphics.fill(x, y + h - BORDER, x + w, y + h, borderColor);
		guiGraphics.fill(x, y + BORDER, x + BORDER, y + h - BORDER, borderColor);
		guiGraphics.fill(x + w - BORDER, y + BORDER, x + w, y + h - BORDER, borderColor);
	}

	private int getBorderColor() {
		if (isEnderChest) {
			return getEnderGlowColor();
		}
		if (color == null) {
			return DEFAULT_BORDER_COLOR;
		}
		int rgb = color.getTextureDiffuseColor();
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		return ARGB.colorFromFloat(0.8F, r / 255.0F, g / 255.0F, b / 255.0F);
	}

	/** Period of the ender-glow color sweep in milliseconds (2.4 s full cycle). */
	private static final long ENDER_GLOW_PERIOD_MS = 2400L;
	/** Phase offset for the green channel — 2π/3 radians. Positions the green
	 *  sine wave 120° behind red so the three channels form a balanced triad. */
	private static final float PHASE_OFFSET_GREEN = (float) (2.0 * Math.PI / 3.0);
	/** Phase offset for the blue channel — 4π/3 radians (equivalently -2π/3). */
	private static final float PHASE_OFFSET_BLUE = (float) (4.0 * Math.PI / 3.0);

	/**
	 * Animated ender-glow border: slowly cycles through cyan → magenta → violet,
	 * matching the ender-eye aesthetic. Keeps the tooltip border visually distinct
	 * from dyed shulker boxes.
	 */
	private static int getEnderGlowColor() {
		float phase = (System.currentTimeMillis() % ENDER_GLOW_PERIOD_MS) / (float) ENDER_GLOW_PERIOD_MS;
		float angle = phase * (float) (Math.PI * 2.0);
		// Desaturated ender palette — baselines close to mid-gray, small swing
		float r = 0.32F + 0.18F * (float) Math.sin(angle);
		float g = 0.38F + 0.16F * (float) Math.sin(angle + PHASE_OFFSET_GREEN);
		float b = 0.48F + 0.18F * (float) Math.sin(angle + PHASE_OFFSET_BLUE);
		return ARGB.colorFromFloat(0.75F,
				Math.clamp(r, 0F, 1F),
				Math.clamp(g, 0F, 1F),
				Math.clamp(b, 0F, 1F));
	}

	private void drawSelectedItemName(Font font, GuiGraphicsExtractor guiGraphics, int x, int y, int width) {
		if (!HandyShulkerConfig.get().showItemName) return;
		int selectedGridIndex = getSelectedGridIndex();
		if (selectedGridIndex < 0 || selectedGridIndex >= items.size()) return;

		ItemStack selectedStack = items.get(selectedGridIndex);
		if (selectedStack.isEmpty()) return;

		// Get full tooltip lines (includes enchantments, attributes, etc.)
		Minecraft mc = Minecraft.getInstance();
		List<Component> lines = new ArrayList<>(selectedStack.getTooltipLines(
				Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.NORMAL
		));

		if (lines.isEmpty()) return;

		// Replace first line (item name) with our version that includes total count
		int totalCount = getTotalCount(selectedStack);
		if (totalCount > selectedStack.getCount()) {
			lines.set(0, Component.empty()
					.append(selectedStack.getStyledHoverName())
					.append(Component.literal(" x" + totalCount).withColor(0xAAAAAA)));
		}

		// Convert to ClientTooltipComponents
		List<ClientTooltipComponent> tooltipComponents = new ArrayList<>();
		for (Component line : lines) {
			tooltipComponents.add(ClientTooltipComponent.create(line.getVisualOrderText()));
		}

		int maxWidth = 0;
		for (Component line : lines) {
			maxWidth = Math.max(maxWidth, font.width(line.getVisualOrderText()));
		}
		int centerX = x + width / 2 - 12;
		guiGraphics.tooltip(
				font,
				tooltipComponents,
				centerX - maxWidth / 2,
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
