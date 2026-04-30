package dev.handy.mods.handyshulker.client.config;

import dev.handy.mods.handyshulker.config.CompactModeKey;
import dev.handy.mods.handyshulker.config.HandyShulkerConfig;
import dev.handy.mods.handyshulker.config.TooltipSize;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Builds the YACL config screen with 3 categories:
 * Features, Sounds, and Tooltip.
 */
public class HandyShulkerConfigScreen {

	public static Screen create(Screen parent) {
		HandyShulkerConfig config = HandyShulkerConfig.get();

		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("config.handyshulker.title"))

				// -- Features --
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("config.handyshulker.category.features"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulker.enableClickInsert"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulker.enableClickInsert.desc")))
								.binding(true, () -> config.enableClickInsert, val -> config.enableClickInsert = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulker.enableScrollExtract"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulker.enableScrollExtract.desc")))
								.binding(true, () -> config.enableScrollExtract, val -> config.enableScrollExtract = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulker.enableEnderChestSupport"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulker.enableEnderChestSupport.desc")))
								.binding(true, () -> config.enableEnderChestSupport, val -> config.enableEnderChestSupport = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulker.showFullnessBar"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulker.showFullnessBar.desc")))
								.binding(true, () -> config.showFullnessBar, val -> config.showFullnessBar = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulker.showColoredBorders"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulker.showColoredBorders.desc")))
								.binding(true, () -> config.showColoredBorders, val -> config.showColoredBorders = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.build())

				// -- Sounds --
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("config.handyshulker.category.sounds"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulker.enableSounds"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulker.enableSounds.desc")))
								.binding(true, () -> config.enableSounds, val -> config.enableSounds = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Float>createBuilder()
								.name(Component.translatable("config.handyshulker.soundVolume"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulker.soundVolume.desc")))
								.binding(1.0f, () -> config.soundVolume, val -> config.soundVolume = val)
								.controller(opt -> FloatSliderControllerBuilder.create(opt)
										.range(0.0f, 1.0f)
										.step(0.05f))
								.build())
						.build())

				// -- Tooltip --
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("config.handyshulker.category.tooltip"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulker.defaultCompactMode"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulker.defaultCompactMode.desc")))
								.binding(false, () -> config.defaultCompactMode, val -> config.defaultCompactMode = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<CompactModeKey>createBuilder()
								.name(Component.translatable("config.handyshulker.compactModeKey"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulker.compactModeKey.desc")))
								.binding(CompactModeKey.SHIFT, () -> config.compactModeKey, val -> config.compactModeKey = val)
								.controller(opt -> EnumControllerBuilder.create(opt)
										.enumClass(CompactModeKey.class)
										.formatValue(val -> Component.translatable(
												"config.handyshulker.compactModeKey." + val.name().toLowerCase())))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulker.showAllSlots"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulker.showAllSlots.desc")))
								.binding(false, () -> config.showAllSlots, val -> config.showAllSlots = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulker.showItemName"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulker.showItemName.desc")))
								.binding(true, () -> config.showItemName, val -> config.showItemName = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handyshulker.showItemCounts"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulker.showItemCounts.desc")))
								.binding(true, () -> config.showItemCounts, val -> config.showItemCounts = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<TooltipSize>createBuilder()
								.name(Component.translatable("config.handyshulker.tooltipSize"))
								.description(OptionDescription.of(
										Component.translatable("config.handyshulker.tooltipSize.desc")))
								.binding(TooltipSize.LARGE, () -> config.tooltipSize, val -> config.tooltipSize = val)
								.controller(opt -> EnumControllerBuilder.create(opt)
										.enumClass(TooltipSize.class)
										.formatValue(val -> Component.translatable(
												"config.handyshulker.tooltipSize." + val.name().toLowerCase())))
								.build())
						.build())

				.save(HandyShulkerConfig::save)
				.build()
				.generateScreen(parent);
	}
}
