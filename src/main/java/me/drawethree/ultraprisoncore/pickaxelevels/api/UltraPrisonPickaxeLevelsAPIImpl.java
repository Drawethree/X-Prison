package me.drawethree.ultraprisoncore.pickaxelevels.api;

import me.drawethree.ultraprisoncore.pickaxelevels.UltraPrisonPickaxeLevels;
import me.drawethree.ultraprisoncore.pickaxelevels.model.PickaxeLevel;
import org.bukkit.inventory.ItemStack;

public class UltraPrisonPickaxeLevelsAPIImpl implements UltraPrisonPickaxeLevelsAPI {

	private UltraPrisonPickaxeLevels plugin;

	public UltraPrisonPickaxeLevelsAPIImpl(UltraPrisonPickaxeLevels plugin) {
		this.plugin = plugin;
	}

	@Override
	public PickaxeLevel getPickaxeLevel(ItemStack item) {
		return null;
	}
}
