package dev.handy.mods.handyshulker;

import dev.handy.mods.handyshulker.config.HandyShulkerConfig;
import dev.handy.mods.handyshulker.net.HandyShulkerPayloads;
import dev.handy.mods.handyshulker.net.SyncEnderChestPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handy Shulker — gives shulker boxes (and ender chests) bundle-like
 * inventory interactions.
 */
public class HandyShulker implements ModInitializer {

	public static final String MOD_ID = "handyshulker";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		HandyShulkerConfig.load();
		HandyShulkerPayloads.registerAll();

		// Seed the client with ender chest contents on join so the tooltip is
		// accurate before the player opens an ender chest for the first time.
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.player;
			ServerPlayNetworking.send(player, new SyncEnderChestPayload(EnderChestHelper.snapshot(player)));
		});

		LOGGER.info("Handy Shulker loaded!");
	}

	/**
	 * Push the current ender chest snapshot to the given server player. Called
	 * after any server-side mutation triggered by this mod, so the client
	 * tooltip reflects reality immediately.
	 */
	public static void syncEnderChest(ServerPlayer player) {
		ServerPlayNetworking.send(player, new SyncEnderChestPayload(EnderChestHelper.snapshot(player)));
	}
}
