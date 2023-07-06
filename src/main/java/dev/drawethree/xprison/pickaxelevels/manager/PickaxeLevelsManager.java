package dev.drawethree.xprison.pickaxelevels.manager;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.pickaxelevels.XPrisonPickaxeLevels;
import dev.drawethree.xprison.pickaxelevels.model.PickaxeLevel;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import dev.drawethree.xprison.utils.item.PrisonItem;
import dev.drawethree.xprison.utils.misc.ProgressBar;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PickaxeLevelsManager {

	private final XPrisonPickaxeLevels plugin;

	public PickaxeLevelsManager(XPrisonPickaxeLevels plugin) {
		this.plugin = plugin;
	}

	public Optional<PickaxeLevel> getNextPickaxeLevel(PickaxeLevel currentLevel) {
		if (currentLevel == null || currentLevel == getMaxLevel()) {
			return Optional.empty();
		}
		return this.getPickaxeLevel(currentLevel.getLevel() + 1);
	}

	private PickaxeLevel getMaxLevel() {
		return this.plugin.getPickaxeLevelsConfig().getMaxLevel();
	}


	public Optional<PickaxeLevel> getPickaxeLevel(int level) {
		return this.plugin.getPickaxeLevelsConfig().getPickaxeLevel(level);
	}

	public Optional<PickaxeLevel> getPickaxeLevel(ItemStack itemStack) {
		if (itemStack == null || !this.plugin.getCore().isPickaxeSupported(itemStack.getType())) {
			return Optional.empty();
		}

		final Integer level = new PrisonItem(itemStack).getLevel();
		return level != null ? this.getPickaxeLevel(level) : Optional.of(getDefaultLevel());
	}

	private PickaxeLevel getDefaultLevel() {
		return this.plugin.getPickaxeLevelsConfig().getDefaultLevel();
	}

	public ItemStack setPickaxeLevel(ItemStack item, PickaxeLevel level, Player p) {

		if (level == null || level.getLevel() <= 0 || level.getLevel() > this.getMaxLevel().getLevel()) {
			return item;
		}

		final PrisonItem prisonItem = new PrisonItem(item);
		prisonItem.setLevel(level.getLevel());
		ItemStackBuilder builder = ItemStackBuilder.of(prisonItem.loadCopy());
		if (level.getDisplayName() != null && !level.getDisplayName().isEmpty()) {
			builder = builder.name(this.getDisplayName(level, p));
		}

		item = builder.build();
		item = this.updatePickaxe(p, item);
		return item;
	}

	private ItemStack updatePickaxe(Player p, ItemStack item) {
		return this.plugin.getCore().getEnchants().getEnchantsManager().updatePickaxe(p, item);
	}

	public ItemStack addDefaultPickaxeLevel(ItemStack item, Player p) {
		return setPickaxeLevel(item, this.getDefaultLevel(), p);
	}


	public ItemStack findPickaxe(Player p) {
		for (ItemStack i : p.getInventory()) {
			if (i == null) {
				continue;
			}
			if (this.plugin.getCore().isPickaxeSupported(i.getType())) {
				return i;
			}
		}
		return null;
	}

	public String getProgressBar(Player player) {
		ItemStack pickaxe = findPickaxe(player);
		return this.getProgressBar(pickaxe);
	}

	public String getProgressBar(ItemStack item) {

		Optional<PickaxeLevel> currentLevelOptional = this.getPickaxeLevel(item);

		double current = 0;
		double required = 1;

		if (currentLevelOptional.isPresent()) {
			PickaxeLevel currentLevel = currentLevelOptional.get();
			Optional<PickaxeLevel> nextLevelOptional = this.getNextPickaxeLevel(currentLevel);
			current = this.getBlocksBroken(item) - currentLevel.getBlocksRequired();
			if (nextLevelOptional.isPresent()) {
				PickaxeLevel nextLevel = nextLevelOptional.get();
				required = nextLevel.getBlocksRequired() - currentLevel.getBlocksRequired();
			}
		}
		return ProgressBar.getProgressBar(this.getProgressBarLength(), this.getProgressBarDelimiter(), current, required);
	}

	public long getBlocksBroken(ItemStack item) {

		if (item == null || item.getType() == Material.AIR) {
			return 0;
		}

		return new PrisonItem(item).getBrokenBlocks();
	}

	private String getProgressBarDelimiter() {
		return this.plugin.getPickaxeLevelsConfig().getProgressBarDelimiter();
	}

	private int getProgressBarLength() {
		return this.plugin.getPickaxeLevelsConfig().getProgressBarLength();
	}

	public void giveRewards(PickaxeLevel level, Player p) {
		if (!Bukkit.isPrimaryThread()) {
			Schedulers.sync().run(() -> level.getRewards().forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName()))));
		} else {
			level.getRewards().forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName())));
		}
	}

	public String getDisplayName(PickaxeLevel level, Player p) {
		if (XPrison.getInstance().isPlaceholderAPIEnabled()) {
			return PlaceholderAPI.setPlaceholders(p, level.getDisplayName()).replace("%player%", p.getName());
		}
		return level.getDisplayName().replace("%player%", p.getName());
	}

	public Optional<PickaxeLevel> getPickaxeLevel(Player player) {
		ItemStack item = this.findPickaxe(player);
		return this.getPickaxeLevel(item);
	}

	public void updatePickaxeLevel(Player player, ItemStack pickaxe) {
		long currentBlocks = this.plugin.getPickaxeLevelsManager().getBlocksBroken(pickaxe);

		Optional<PickaxeLevel> currentLevelOptional = this.getPickaxeLevel(pickaxe);

		if (!currentLevelOptional.isPresent()) {
			return;
		}

		PickaxeLevel currentLevel = currentLevelOptional.get();
		Optional<PickaxeLevel> nextLevelOptional = this.getNextPickaxeLevel(currentLevel);

		List<PickaxeLevel> toGive = new ArrayList<>();

		while (nextLevelOptional.isPresent()) {
			PickaxeLevel nextLevel = nextLevelOptional.get();
			if (currentBlocks < nextLevel.getBlocksRequired()) {
				break;
			}
			toGive.add(nextLevel);
			nextLevelOptional = this.getNextPickaxeLevel(nextLevel);
		}

		if (!toGive.isEmpty()) {
			toGive.forEach(pickaxeLevel -> this.giveRewards(pickaxeLevel, player));
			ItemStack updatedPickaxe = this.setPickaxeLevel(pickaxe, toGive.get(toGive.size() - 1), player);
			player.setItemInHand(updatedPickaxe);
			PlayerUtils.sendMessage(player, this.plugin.getPickaxeLevelsConfig().getMessage("pickaxe-level-up").replace("%level%", String.valueOf(toGive.get(toGive.size() - 1).getLevel())));
		}
	}
}
