package dev.drawethree.ultraprisoncore.pickaxelevels.api;

import dev.drawethree.ultraprisoncore.pickaxelevels.UltraPrisonPickaxeLevels;
import dev.drawethree.ultraprisoncore.pickaxelevels.model.PickaxeLevel;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class UltraPrisonPickaxeLevelsAPIImpl implements UltraPrisonPickaxeLevelsAPI {

	private final UltraPrisonPickaxeLevels plugin;

	public UltraPrisonPickaxeLevelsAPIImpl(UltraPrisonPickaxeLevels plugin) {
		this.plugin = plugin;
	}

	@Override
	public PickaxeLevel getPickaxeLevel(ItemStack item) {
		return this.plugin.getPickaxeLevel(item);
	}

	@Override
	public PickaxeLevel getPickaxeLevel(Player player) {
		ItemStack item = this.plugin.findPickaxe(player);
		return this.getPickaxeLevel(item);
	}

	@Override
	public PickaxeLevel getPickaxeLevel(int level) {
		return plugin.getPickaxeLevel(level);
	}

	@Override
	public void setPickaxeLevel(Player player, ItemStack item, PickaxeLevel level) {
		this.plugin.setPickaxeLevel(item, level, player);
	}

	@Override
	public void setPickaxeLevel(Player player, ItemStack item, int level) {
		PickaxeLevel pickaxeLevel = getPickaxeLevel(level);
		this.plugin.setPickaxeLevel(item, pickaxeLevel, player);

	}
}
