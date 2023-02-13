package dev.drawethree.xprison.pickaxelevels.api;

import dev.drawethree.xprison.pickaxelevels.manager.PickaxeLevelsManager;
import dev.drawethree.xprison.pickaxelevels.model.PickaxeLevel;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class XPrisonPickaxeLevelsAPIImpl implements XPrisonPickaxeLevelsAPI {

	private final PickaxeLevelsManager manager;

	public XPrisonPickaxeLevelsAPIImpl(PickaxeLevelsManager manager) {
		this.manager = manager;
	}

	@Override
	public Optional<PickaxeLevel> getPickaxeLevel(ItemStack item) {
		return this.manager.getPickaxeLevel(item);
	}

	@Override
	public Optional<PickaxeLevel> getPickaxeLevel(Player player) {
		return this.manager.getPickaxeLevel(player);
	}

	@Override
	public Optional<PickaxeLevel> getPickaxeLevel(int level) {
		return this.manager.getPickaxeLevel(level);
	}

	@Override
	public void setPickaxeLevel(Player player, ItemStack item, PickaxeLevel level) {
		this.manager.setPickaxeLevel(item, level, player);
	}

	@Override
	public void setPickaxeLevel(Player player, ItemStack item, int level) {
		Optional<PickaxeLevel> pickaxeLevelOptional = getPickaxeLevel(level);
		pickaxeLevelOptional.ifPresent(pickaxeLevel -> this.manager.setPickaxeLevel(item, pickaxeLevel, player));
	}

	@Override
	public String getProgressBar(Player player) {
		return this.manager.getProgressBar(player);
	}
}
