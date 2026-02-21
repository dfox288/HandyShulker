package dev.handyshulkers.mixin;

import dev.handyshulkers.ShulkerBoxHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * Suppresses the vanilla text tooltip for shulker box contents.
 * Our custom visual grid tooltip (ShulkerTooltip) replaces it,
 * so the text list ("Spruce Log x64", etc.) is redundant.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
	private <T extends TooltipProvider> void handyshulkers$suppressContainerTooltip(
			DataComponentType<T> dataComponentType,
			Item.TooltipContext tooltipContext,
			TooltipDisplay tooltipDisplay,
			Consumer<Component> consumer,
			TooltipFlag tooltipFlag,
			CallbackInfo ci) {

		if (dataComponentType == DataComponents.CONTAINER) {
			ItemStack self = (ItemStack) (Object) this;
			if (ShulkerBoxHelper.isShulkerBox(self)) {
				ci.cancel();
			}
		}
	}
}
