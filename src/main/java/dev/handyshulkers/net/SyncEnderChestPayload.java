package dev.handyshulkers.net;

import dev.handyshulkers.HandyShulkers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * S2C payload that pushes a snapshot of the player's ender chest inventory.
 *
 * Sent on join (so fresh clients have real data before hovering any ender
 * chest) and after any mutation driven by this mod's click-to-insert/extract
 * behavior. Player-driven mutations through the vanilla container menu already
 * sync via the standard slot-update packets, so those don't need a separate
 * push.
 */
public record SyncEnderChestPayload(List<ItemStack> items) implements CustomPacketPayload {

	public static final Type<SyncEnderChestPayload> ID =
			new Type<>(Identifier.fromNamespaceAndPath(HandyShulkers.MOD_ID, "sync_ender_chest"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncEnderChestPayload> CODEC =
			ItemStack.OPTIONAL_STREAM_CODEC
					.apply(ByteBufCodecs.list())
					.map(SyncEnderChestPayload::new, SyncEnderChestPayload::items);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
