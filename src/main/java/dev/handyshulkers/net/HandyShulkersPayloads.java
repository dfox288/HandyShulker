package dev.handyshulkers.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * Registers HandyShulker's custom network payloads. Called from both the main
 * and client entrypoints; idempotent since the registry deduplicates by ID.
 */
public final class HandyShulkersPayloads {

	private HandyShulkersPayloads() {}

	private static boolean registered;

	public static synchronized void registerAll() {
		if (registered) return;
		PayloadTypeRegistry.clientboundPlay().register(SyncEnderChestPayload.ID, SyncEnderChestPayload.CODEC);
		registered = true;
	}
}
