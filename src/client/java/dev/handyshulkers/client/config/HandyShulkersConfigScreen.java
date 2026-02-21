package dev.handyshulkers.client.config;

import dev.handyshulkers.config.HandyShulkersConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Builds the YACL config screen with 3 categories:
 * Features, Sounds, and Tooltip.
 */
@Environment(EnvType.CLIENT)
public class HandyShulkersConfigScreen {

	public static Screen create(Screen parent) {
		HandyShulkersConfig config = HandyShulkersConfig.get();

		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("config.handyshulkers.title"))

				// -- Features --
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("config.handyshulkers.category.features"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulkers.enableClickInsert"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulkers.enableClickInsert.desc")))
								.binding(true, () -> config.enableClickInsert, val -> config.enableClickInsert = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulkers.enableScrollExtract"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulkers.enableScrollExtract.desc")))
								.binding(true, () -> config.enableScrollExtract, val -> config.enableScrollExtract = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulkers.showFullnessBar"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulkers.showFullnessBar.desc")))
								.binding(true, () -> config.showFullnessBar, val -> config.showFullnessBar = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulkers.showColoredBorders"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulkers.showColoredBorders.desc")))
								.binding(true, () -> config.showColoredBorders, val -> config.showColoredBorders = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.build())

				// -- Sounds --
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("config.handyshulkers.category.sounds"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulkers.enableSounds"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulkers.enableSounds.desc")))
								.binding(true, () -> config.enableSounds, val -> config.enableSounds = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Float>createBuilder()
								.name(Component.translatable("config.handyshulkers.soundVolume"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulkers.soundVolume.desc")))
								.binding(1.0f, () -> config.soundVolume, val -> config.soundVolume = val)
								.controller(opt -> FloatSliderControllerBuilder.create(opt)
										.range(0.0f, 1.0f)
										.step(0.05f))
								.build())
						.build())

				// -- Tooltip --
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("config.handyshulkers.category.tooltip"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulkers.defaultCompactMode"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulkers.defaultCompactMode.desc")))
								.binding(false, () -> config.defaultCompactMode, val -> config.defaultCompactMode = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulkers.showItemName"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulkers.showItemName.desc")))
								.binding(true, () -> config.showItemName, val -> config.showItemName = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulkers.showItemCounts"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulkers.showItemCounts.desc")))
								.binding(true, () -> config.showItemCounts, val -> config.showItemCounts = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.build())

				.save(HandyShulkersConfig::save)
				.build()
				.generateScreen(parent);
	}
}
