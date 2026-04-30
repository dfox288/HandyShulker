package dev.handy.mods.handyshulker.mixin;

import dev.handy.mods.handyshulker.HandyShulker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Push the player's full ender chest snapshot to the client whenever they
 * finish interacting with a placed ender chest block.
 *
 * The vanilla chest menu uses a separate client-side container for rendering,
 * so closing the menu leaves {@code LocalPlayer.getEnderChestInventory()} at
 * whatever state it had before the interaction — typically the join snapshot
 * or empty. Without this hook the tooltip goes stale the first time the player
 * touches an ender chest after joining.
 */
@Mixin(PlayerEnderChestContainer.class)
public abstract class PlayerEnderChestContainerMixin {

	@Inject(method = "stopOpen(Lnet/minecraft/world/entity/ContainerUser;)V", at = @At("TAIL"))
	private void handyshulker$syncOnClose(ContainerUser user, CallbackInfo ci) {
		if (user.getLivingEntity() instanceof ServerPlayer player) {
			HandyShulker.syncEnderChest(player);
		}
	}
}
