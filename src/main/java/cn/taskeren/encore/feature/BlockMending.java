/*
 * Copyright (c) 2024 Taskeren and Contributors - All Rights Reserved.
 */

package cn.taskeren.encore.feature;

import cn.taskeren.encore.Encore;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlockMending extends Feature {

	public List<Material> blockingMaterials;

	public BlockMending(Encore encore) {
		super("block-mending", encore);
		this.blockingMaterials = new ArrayList<>();
		registerAsListener();
		syncEnablingStatus();
	}

	private static final String CONFIG_BLOCKING_MATERIALS_PATH = "feature.block-mending-materials";

	private void loadConfig() {
		var newBlockingMaterials = getConfig().getStringList(CONFIG_BLOCKING_MATERIALS_PATH)
				.stream().map(Material::getMaterial).toList(); // read config and map to materials
		// show the config values
		if(!newBlockingMaterials.isEmpty()) {
			getLogger().info("Read block-mending materials from config:");
			newBlockingMaterials.forEach(m -> getLogger().info("- {}", m));
		} else {
			getLogger().info("Read block-mending materials is empty! Please consider to fill in the values in config.yml at '"+CONFIG_BLOCKING_MATERIALS_PATH+"'.");
		}
		// replace the list
		this.blockingMaterials = newBlockingMaterials;
		// refresh the config values
		saveConfig();
	}

	private void saveConfig() {
		var blockingMaterialsToString = blockingMaterials
				.stream().map(Material::name).toList(); // serialize to names
		getConfig().set(CONFIG_BLOCKING_MATERIALS_PATH, blockingMaterialsToString);
		getEncore().saveConfig();
	}

	@Override
	public void onEncoreEnabled() {
		loadConfig();
	}

	@Override
	public void onEncoreReload() {
		loadConfig();
	}

	@EventHandler
	public void onAnvil(PrepareAnvilEvent event) {
		// skip if not enabled
		if(!isEnabled()) return;

		var inv = event.getInventory();

		var level1 = getEnchantedBookMendingLevel(inv.getFirstItem());
		var level2 = getEnchantedBookMendingLevel(inv.getSecondItem());

		if(level1 > 0 || level2 > 0) {
			if(isBlockingMaterials(inv.getFirstItem())) {
				event.setResult(null);
			}
		}
	}

	/**
	 * Get the Mending level in Enchanted Book, or -1 if not have Mending.
	 *
	 * @param item the item
	 * @return the Mending level or -1
	 */
	private int getEnchantedBookMendingLevel(@Nullable ItemStack item) {
		// if the item is null or not enchanted book, return -1
		if(item == null || item.getType() != Material.ENCHANTED_BOOK) return -1;
		return item.getItemMeta() instanceof EnchantmentStorageMeta enchantMeta ? enchantMeta.getStoredEnchantLevel(Enchantment.MENDING) : -1;
	}

	/**
	 * Get if the item is in the blocking list. {@code false} if it is null.
	 *
	 * @param item the item
	 * @return if the item is in the blocking list
	 */
	private boolean isBlockingMaterials(@Nullable ItemStack item) {
		if(item == null) return false;
		if(blockingMaterials.isEmpty()) return false;
		return blockingMaterials.contains(item.getType());
	}

}
