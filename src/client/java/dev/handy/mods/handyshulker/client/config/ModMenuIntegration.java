package dev.handy.mods.handyshulker.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

/**
 * ModMenu integration — provides a config button in the mod list
 * that opens the YACL config screen (if YACL is installed).
 *
 * This class is only loaded by Fabric when ModMenu is present.
 * YACL is checked at runtime so the mod works without it.
 */
public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
			return parent -> HandyShulkerConfigScreen.create(parent);
		}
		return parent -> null;
	}
}
