package dev.drawethree.xprison.blocks.managers;

import dev.drawethree.xprison.blocks.XPrisonBlocks;
import dev.drawethree.xprison.blocks.model.BlockReward;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Schedulers;
import me.lucko.helper.time.Time;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.error;
import static dev.drawethree.xprison.utils.log.XPrisonLogger.info;

public class BlocksManager {

	private final XPrisonBlocks plugin;
	private final Map<UUID, Long> blocksCache;
	private final Map<UUID, Long> blocksCacheWeekly;

	public BlocksManager(XPrisonBlocks plugin) {
		this.plugin = plugin;
		this.blocksCache = new ConcurrentHashMap<>();
		this.blocksCacheWeekly = new ConcurrentHashMap<>();
	}

	public void savePlayerData(Collection<Player> players, boolean removeFromCache, boolean async) {
		if (async) {
			Schedulers.async().run(() -> savePlayerDataLogic(players, removeFromCache));
		} else {
			savePlayerDataLogic(players, removeFromCache);
		}
	}

	private void savePlayerDataLogic(Collection<Player> players, boolean removeFromCache) {
		for (Player player : players) {
			this.plugin.getBlocksService().setBlocks(player, blocksCache.getOrDefault(player.getUniqueId(), 0L));
			this.plugin.getBlocksService().setBlocksWeekly(player, blocksCacheWeekly.getOrDefault(player.getUniqueId(), 0L));

			if (removeFromCache) {
				this.blocksCache.remove(player.getUniqueId());
				this.blocksCacheWeekly.remove(player.getUniqueId());
			}

			this.plugin.getCore().debug(String.format("Saved player %s blocks broken to database.", player.getName()), this.plugin);
		}
	}

	public void savePlayerDataOnDisable() {
		for (UUID uuid : blocksCache.keySet()) {
			this.plugin.getBlocksService().setBlocks(Players.getOfflineNullable(uuid), blocksCache.get(uuid));
		}
		for (UUID uuid : blocksCache.keySet()) {
			this.plugin.getBlocksService().setBlocksWeekly(Players.getOfflineNullable(uuid), blocksCacheWeekly.get(uuid));
		}
		blocksCache.clear();
		blocksCacheWeekly.clear();
		info("&aBlocks Broken and Weekly Blocks Broken saved.");

	}

	private void loadPlayerDataOnEnable() {
		loadPlayerData(Players.all());
	}

	public void loadPlayerData(Collection<Player> players) {
		Schedulers.async().run(() -> {
			for (Player player : players) {

				this.plugin.getBlocksService().createBlocks(player);
				this.plugin.getBlocksService().createBlocksWeekly(player);

				long playerBlocks = this.plugin.getBlocksService().getPlayerBrokenBlocks(player);
				long playerBlocksWeekly = this.plugin.getBlocksService().getPlayerBrokenBlocksWeekly(player);

				this.blocksCache.put(player.getUniqueId(), playerBlocks);
				this.blocksCacheWeekly.put(player.getUniqueId(), playerBlocksWeekly);

				this.plugin.getCore().debug(String.format("Loaded blocks broken of player %s from database", player.getName()), this.plugin);
			}
		});
	}

	public synchronized long getPlayerBrokenBlocks(OfflinePlayer p) {
		if (!p.isOnline()) {
			return this.plugin.getBlocksService().getPlayerBrokenBlocks(p);
		} else {
			return blocksCache.getOrDefault(p.getUniqueId(), (long) 0);
		}
	}

	public synchronized long getPlayerBrokenBlocksWeekly(OfflinePlayer p) {
		if (!p.isOnline()) {
			return this.plugin.getBlocksService().getPlayerBrokenBlocksWeekly(p);
		} else {
			return blocksCacheWeekly.getOrDefault(p.getUniqueId(), (long) 0);
		}
	}


	public void addBlocksBroken(CommandSender sender, OfflinePlayer player, long amount) {

		if (amount <= 0) {
			if (sender != null) {
				PlayerUtils.sendMessage(sender, "&cPlease specify amount greater than 0!");
			}
			return;
		}

		long finalAmount = amount;

		Schedulers.async().run(() -> {

			long currentBroken = getPlayerBrokenBlocks(player);
			long currentBrokenWeekly = getPlayerBrokenBlocksWeekly(player);

			BlockReward nextReward = this.getNextBlockReward(player);

			if (!player.isOnline()) {
				this.plugin.getBlocksService().setBlocks(player, currentBroken + finalAmount);
				this.plugin.getBlocksService().setBlocksWeekly(player, currentBrokenWeekly + finalAmount);
			} else {
				blocksCache.put(player.getUniqueId(), currentBroken + finalAmount);
				blocksCacheWeekly.put(player.getUniqueId(), currentBrokenWeekly + finalAmount);

				while (nextReward != null && nextReward.getBlocksRequired() <= blocksCache.get(player.getUniqueId())) {
					nextReward.giveTo((Player) player);
					nextReward = this.getNextBlockReward(nextReward);
				}
			}

			if (sender != null && !(sender instanceof ConsoleCommandSender)) {
				PlayerUtils.sendMessage(sender, plugin.getBlocksConfig().getMessage("admin_give_blocks").replace("%player%", player.getName()).replace("%blocks%", String.format("%,d", finalAmount)));
			}
		});
	}

	public void addBlocksBroken(OfflinePlayer player, List<Block> blocks) {
		long finalAmount = blocks.size();

		Schedulers.async().run(() -> {

			long currentBroken = getPlayerBrokenBlocks(player);
			long currentBrokenWeekly = getPlayerBrokenBlocksWeekly(player);

			BlockReward nextReward = this.getNextBlockReward(player);

			if (!player.isOnline()) {
				this.plugin.getBlocksService().setBlocks(player, currentBroken + finalAmount);
				this.plugin.getBlocksService().setBlocksWeekly(player, currentBrokenWeekly + finalAmount);
			} else {
				blocksCache.put(player.getUniqueId(), currentBroken + finalAmount);
				blocksCacheWeekly.put(player.getUniqueId(), currentBrokenWeekly + finalAmount);

				while (nextReward != null && nextReward.getBlocksRequired() <= blocksCache.get(player.getUniqueId())) {
					nextReward.giveTo((Player) player);
					nextReward = this.getNextBlockReward(nextReward);
				}
			}
		});
	}

	private BlockReward getNextBlockReward(BlockReward oldReward) {
		boolean next = false;
		for (long l : this.plugin.getBlockRewardsConfig().getBlockRewards().keySet()) {
			if (next) {
				return this.plugin.getBlockRewardsConfig().getBlockRewards().get(l);
			}
			if (l == oldReward.getBlocksRequired()) {
				next = true;
			}
		}

		return null;
	}

	public void removeBlocksBroken(CommandSender sender, OfflinePlayer player, long amount) {

		if (amount <= 0) {
			PlayerUtils.sendMessage(sender, "&cPlease specify amount greater than 0!");
			return;
		}

		Schedulers.async().run(() -> {

			long currentBroken = getPlayerBrokenBlocks(player);
			long currentBrokenWeekly = getPlayerBrokenBlocksWeekly(player);

			if (!player.isOnline()) {
				this.plugin.getBlocksService().setBlocks(player, currentBroken - amount);
				this.plugin.getBlocksService().setBlocksWeekly(player, currentBrokenWeekly - amount);
			} else {
				blocksCache.put(player.getUniqueId(), currentBroken - amount);
				blocksCacheWeekly.put(player.getUniqueId(), currentBrokenWeekly - amount);
			}

			PlayerUtils.sendMessage(sender, plugin.getBlocksConfig().getMessage("admin_remove_blocks").replace("%player%", player.getName()).replace("%blocks%", String.format("%,d", amount)));

		});
	}

	public void setBlocksBroken(CommandSender sender, OfflinePlayer player, long amount) {

		if (amount < 0) {
			PlayerUtils.sendMessage(sender, "&cPlease specify positive amount!");
			return;
		}

		Schedulers.async().run(() -> {
			BlockReward nextReward = this.getNextBlockReward(player);

			if (!player.isOnline()) {

				this.plugin.getBlocksService().setBlocks(player, amount);
				this.plugin.getBlocksService().setBlocksWeekly(player, amount);
			} else {
				blocksCache.put(player.getUniqueId(), amount);
				blocksCacheWeekly.put(player.getUniqueId(), amount);

				while (nextReward != null && nextReward.getBlocksRequired() <= blocksCache.get(player.getUniqueId())) {
					nextReward.giveTo((Player) player);
					nextReward = this.getNextBlockReward(nextReward);
				}
			}

			PlayerUtils.sendMessage(sender, plugin.getBlocksConfig().getMessage("admin_set_blocks").replace("%player%", player.getName()).replace("%blocks%", String.format("%,d", amount)));
		});
	}

	public void sendBlocksTop(CommandSender sender) {
		List<String> format = this.plugin.getBlocksConfig().getBlocksTopFormat();
		Schedulers.async().run(() -> {
			Map<UUID, Long> topBlocks = this.plugin.getBlocksService().getTopBlocks(this.plugin.getBlocksConfig().getTopPlayersAmount());
			for (String s : format) {
				if (s.startsWith("{FOR_EACH_PLAYER}")) {
					sendBlocksTop(sender, s, topBlocks);
				} else {
					PlayerUtils.sendMessage(sender, s);
				}
			}
		});
	}

	private void sendBlocksTop(CommandSender sender, String s, Map<UUID, Long> top) {
		String rawContent = s.replace("{FOR_EACH_PLAYER} ", "");
		for (int i = 0; i < 10; i++) {
			try {
				UUID uuid = (UUID) top.keySet().toArray()[i];
				OfflinePlayer player = Players.getOfflineNullable(uuid);
				String name;
				if (player.getName() == null) {
					name = "Unknown Player";
				} else {
					name = player.getName();
				}
				long blocks = top.get(uuid);
				PlayerUtils.sendMessage(sender, rawContent.replace("%position%", String.valueOf(i + 1)).replace("%player%", name).replace("%blocks%", String.format("%,d", blocks)));
			} catch (IndexOutOfBoundsException e) {
				break;
			} catch (Exception e) {
				error("Exception during sending BlocksTop to " + sender.getName());
				e.printStackTrace();
			}
		}
	}

	public void sendBlocksTopWeekly(CommandSender sender) {
		List<String> format = this.plugin.getBlocksConfig().getBlocksTopFormatWeekly();

		Schedulers.async().run(() -> {
			Map<UUID, Long> topBlocksWeekly = this.plugin.getBlocksService().getTopBlocksWeekly(this.plugin.getBlocksConfig().getTopPlayersAmount());
			for (String s : format) {
				if (s.startsWith("{FOR_EACH_PLAYER}")) {
					sendBlocksTop(sender, s, topBlocksWeekly);
				} else {
					PlayerUtils.sendMessage(sender, s);
				}
			}
		});
	}

	public BlockReward getNextBlockReward(OfflinePlayer p) {
		long blocksBroken = this.getPlayerBrokenBlocks(p);
		for (long l : this.plugin.getBlockRewardsConfig().getBlockRewards().keySet()) {
			if (l > blocksBroken) {
				return this.plugin.getBlockRewardsConfig().getBlockRewards().get(l);
			}
		}
		return null;
	}

	public void resetBlocksTopWeekly(CommandSender sender) {
		PlayerUtils.sendMessage(sender, "&7&oStarting to reset BlocksTop - Weekly. This may take a while...");
		this.plugin.getBlocksConfig().setNextResetWeekly(Time.nowMillis() + TimeUnit.DAYS.toMillis(7));
		this.plugin.getBlocksService().resetBlocksWeekly();
		PlayerUtils.sendMessage(sender, "&aBlocksTop - Weekly - Reset!");
	}

	private void saveWeeklyReset() {
		this.plugin.getBlocksConfig().getYamlConfig().set("next-reset-weekly", this.plugin.getBlocksConfig().getNextResetWeekly());
		this.plugin.getBlocksConfig().save();
	}

	public void reload() {

	}

	public void disable() {
		this.saveWeeklyReset();
		this.savePlayerDataOnDisable();
	}

	public void sendInfoMessage(CommandSender sender, OfflinePlayer target) {
		Schedulers.async().run(() -> {
			if (sender == target) {
				PlayerUtils.sendMessage(sender, plugin.getBlocksConfig().getMessage("your_blocks").replace("%blocks%", String.format("%,d", this.getPlayerBrokenBlocks(target))));
			} else {
				PlayerUtils.sendMessage(sender, plugin.getBlocksConfig().getMessage("other_blocks").replace("%blocks%", String.format("%,d", this.getPlayerBrokenBlocks(target))).replace("%player%", target.getName()));
			}
		});
	}


	public void handleBlockBreak(Player p, List<Block> blocks, boolean countBlocksBroken) {

		long startTime = System.currentTimeMillis();

		if (countBlocksBroken) {
			this.addBlocksBroken(p, blocks);
		}

		this.plugin.getCore().debug("XPrisonBlocks::handleBlockBreak >> Took " + (System.currentTimeMillis() - startTime) + " ms.", this.plugin);
	}

	public void enable() {
		if (this.checkBlocksTopWeeklyReset()) {
			resetBlocksTopWeekly(Bukkit.getConsoleSender());
		}
		this.loadPlayerDataOnEnable();
	}

	private boolean checkBlocksTopWeeklyReset() {
		long nextResetWeeklyMillis = this.plugin.getBlocksConfig().getNextResetWeekly();
		return Time.nowMillis() >= nextResetWeeklyMillis;
	}
}
