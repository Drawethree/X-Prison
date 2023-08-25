package dev.drawethree.xprison.tokens.managers;

import dev.drawethree.xprison.api.enums.LostCause;
import dev.drawethree.xprison.api.enums.ReceiveCause;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import dev.drawethree.xprison.tokens.api.events.PlayerTokensLostEvent;
import dev.drawethree.xprison.tokens.api.events.PlayerTokensReceiveEvent;
import dev.drawethree.xprison.tokens.api.events.XPrisonBlockBreakEvent;
import dev.drawethree.xprison.tokens.model.BlockReward;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import dev.drawethree.xprison.utils.item.PrisonItem;
import dev.drawethree.xprison.utils.misc.NumberUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.time.Time;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class TokensManager {

	private final XPrisonTokens plugin;
	private final Map<UUID, Long> tokensCache;
	private final Map<UUID, Long> blocksCache;
	private final Map<UUID, Long> blocksCacheWeekly;
	private final List<UUID> tokenMessageOnPlayers;

	public TokensManager(XPrisonTokens plugin) {
		this.plugin = plugin;
		this.tokenMessageOnPlayers = new ArrayList<>();
		this.tokensCache = new ConcurrentHashMap<>();
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
			this.plugin.getTokensService().setTokens(player, tokensCache.getOrDefault(player.getUniqueId(), 0L));
			this.plugin.getBlocksService().setBlocks(player, blocksCache.getOrDefault(player.getUniqueId(), 0L));
			this.plugin.getBlocksService().setBlocksWeekly(player, blocksCacheWeekly.getOrDefault(player.getUniqueId(), 0L));

			if (removeFromCache) {
				this.tokensCache.remove(player.getUniqueId());
				this.blocksCache.remove(player.getUniqueId());
				this.blocksCacheWeekly.remove(player.getUniqueId());
			}

			this.plugin.getCore().debug(String.format("Saved player %s tokens & blocks broken to database.", player.getName()), this.plugin);
		}
	}

	public void savePlayerDataOnDisable() {
		for (UUID uuid : blocksCache.keySet()) {
			this.plugin.getBlocksService().setBlocks(Players.getOfflineNullable(uuid), blocksCache.get(uuid));
		}
		for (UUID uuid : tokensCache.keySet()) {
			this.plugin.getTokensService().setTokens(Players.getOfflineNullable(uuid), tokensCache.get(uuid));
		}
		for (UUID uuid : blocksCache.keySet()) {
			this.plugin.getBlocksService().setBlocksWeekly(Players.getOfflineNullable(uuid), blocksCacheWeekly.get(uuid));
		}
		tokensCache.clear();
		blocksCache.clear();
		blocksCacheWeekly.clear();
		this.plugin.getCore().getLogger().info("Saved online player tokens, blocks broken and weekly blocks broken.");

	}

	private void loadPlayerDataOnEnable() {
		loadPlayerData(Players.all());
	}

	public void loadPlayerData(Collection<Player> players) {
		Schedulers.async().run(() -> {
			for (Player player : players) {

				this.plugin.getTokensService().createTokens(player, this.plugin.getTokensConfig().getStartingTokens());
				this.plugin.getBlocksService().createBlocks(player);
				this.plugin.getBlocksService().createBlocksWeekly(player);

				long playerTokens = this.plugin.getTokensService().getTokens(player);
				long playerBlocks = this.plugin.getBlocksService().getPlayerBrokenBlocks(player);
				long playerBlocksWeekly = this.plugin.getBlocksService().getPlayerBrokenBlocksWeekly(player);

				this.tokensCache.put(player.getUniqueId(), playerTokens);
				this.blocksCache.put(player.getUniqueId(), playerBlocks);
				this.blocksCacheWeekly.put(player.getUniqueId(), playerBlocksWeekly);

				this.plugin.getCore().debug(String.format("Loaded tokens and blocks broken of player %s from database", player.getName()), this.plugin);
			}
		});
	}

	public void setTokens(OfflinePlayer p, long newAmount, CommandSender executor) {
		Schedulers.async().run(() -> {
			if (!p.isOnline()) {
				this.plugin.getTokensService().setTokens(p, newAmount);
			} else {
				tokensCache.put(p.getUniqueId(), newAmount);
			}
			PlayerUtils.sendMessage(executor, plugin.getTokensConfig().getMessage("admin_set_tokens").replace("%player%", p.getName()).replace("%tokens%", String.format("%,d", newAmount)));
		});
	}

	public void giveTokens(OfflinePlayer p, long amount, CommandSender executor, ReceiveCause cause) {
		long currentTokens = getPlayerTokens(p);

		this.plugin.getCore().debug("XPrisonPlayerTokenReceiveEvent :: Player Tokens :: " + currentTokens, this.plugin);

		long finalAmount = this.callTokensReceiveEvent(cause, p, amount);

		this.plugin.getCore().debug("XPrisonPlayerTokenReceiveEvent :: Final amount :: " + finalAmount, this.plugin);

		long newAmount;

		if (NumberUtils.wouldAdditionBeOverMaxLong(currentTokens, finalAmount)) {
			newAmount = Long.MAX_VALUE;
		} else {
			newAmount = currentTokens + finalAmount;
		}

		if (!p.isOnline()) {
			this.plugin.getTokensService().setTokens(p, newAmount);
		} else {
			tokensCache.put(p.getUniqueId(), newAmount);
			if (executor instanceof ConsoleCommandSender && !this.hasOffTokenMessages(p.getPlayer())) {
				PlayerUtils.sendMessage(p.getPlayer(), plugin.getTokensConfig().getMessage("tokens_received_console").replace("%tokens%", String.format("%,d", finalAmount)).replace("%player%", executor == null ? "Console" : executor.getName()));
			} else if (cause == ReceiveCause.MINING && !this.hasOffTokenMessages(p.getPlayer())) {
				PlayerUtils.sendMessage(p.getPlayer(), this.plugin.getTokensConfig().getMessage("tokens_received_mining").replace("%amount%", String.format("%,d", finalAmount)));
			} else if (cause == ReceiveCause.LUCKY_BLOCK && !this.hasOffTokenMessages(p.getPlayer())) {
				PlayerUtils.sendMessage(p.getPlayer(), this.plugin.getTokensConfig().getMessage("lucky_block_mined").replace("%amount%", String.format("%,d", finalAmount)));
			}
		}

		this.plugin.getCore().debug("XPlayerTokenReceiveEvent :: Player tokens final  :: " + this.tokensCache.getOrDefault(p.getUniqueId(), 0L), this.plugin);

		if (executor != null && !(executor instanceof ConsoleCommandSender)) {
			PlayerUtils.sendMessage(executor, plugin.getTokensConfig().getMessage("admin_give_tokens").replace("%player%", p.getName()).replace("%tokens%", String.format("%,d", finalAmount)));
		}
	}

	private long callTokensReceiveEvent(ReceiveCause cause, OfflinePlayer p, long amount) {
		PlayerTokensReceiveEvent event = new PlayerTokensReceiveEvent(cause, p, amount);

		Events.callSync(event);

		if (event.isCancelled()) {
			return amount;
		}

		return event.getAmount();
	}

	public void redeemTokens(Player p, ItemStack item, boolean shiftClick, boolean offhand) {
        final Long tokenAmount = new PrisonItem(item).getTokens();
        if (tokenAmount == null) {
            PlayerUtils.sendMessage(p, plugin.getTokensConfig().getMessage("not_token_item"));
            return;
        }
        int itemAmount = item.getAmount();
        if (shiftClick) {
            if (offhand) {
                p.getInventory().setItemInOffHand(null);
            } else {
                p.setItemInHand(null);
            }
            this.giveTokens(p, tokenAmount * itemAmount, null, ReceiveCause.REDEEM);
            PlayerUtils.sendMessage(p, plugin.getTokensConfig().getMessage("tokens_redeem").replace("%tokens%", String.format("%,d", tokenAmount * itemAmount)));
        } else {
            this.giveTokens(p, tokenAmount, null, ReceiveCause.REDEEM);
            if (item.getAmount() == 1) {
                if (offhand) {
                    p.getInventory().setItemInOffHand(null);
                } else {
                    p.setItemInHand(null);
                }
            } else {
                item.setAmount(item.getAmount() - 1);
            }
            PlayerUtils.sendMessage(p, plugin.getTokensConfig().getMessage("tokens_redeem").replace("%tokens%", String.format("%,d", tokenAmount)));
        }
	}

	public void payTokens(Player executor, long amount, OfflinePlayer target) {
		Schedulers.async().run(() -> {
			if (getPlayerTokens(executor) >= amount) {
				this.removeTokens(executor, amount, null, LostCause.PAY);
				this.giveTokens(target, amount, null, ReceiveCause.PAY);
				PlayerUtils.sendMessage(executor, plugin.getTokensConfig().getMessage("tokens_send").replace("%player%", target.getName()).replace("%tokens%", String.format("%,d", amount)));
				if (target.isOnline()) {
					PlayerUtils.sendMessage((CommandSender) target, plugin.getTokensConfig().getMessage("tokens_received").replace("%player%", executor.getName()).replace("%tokens%", String.format("%,d", amount)));
				}
			} else {
				PlayerUtils.sendMessage(executor, plugin.getTokensConfig().getMessage("not_enough_tokens"));
			}
		});
	}

	public void withdrawTokens(Player executor, long amount, int value) {
		Schedulers.async().run(() -> {
			long totalAmount = amount * value;

			if (this.getPlayerTokens(executor) < totalAmount) {
				PlayerUtils.sendMessage(executor, plugin.getTokensConfig().getMessage("not_enough_tokens"));
				return;
			}

			removeTokens(executor, totalAmount, null, LostCause.WITHDRAW);

			ItemStack item = createTokenItem(amount, value);
			Collection<ItemStack> notFit = executor.getInventory().addItem(item).values();

			if (!notFit.isEmpty()) {
				notFit.forEach(itemStack -> {
					this.giveTokens(executor, amount * item.getAmount(), null, ReceiveCause.REDEEM);
				});
			}

			PlayerUtils.sendMessage(executor, plugin.getTokensConfig().getMessage("withdraw_successful").replace("%amount%", String.format("%,d", amount)).replace("%value%", String.format("%,d", value)));
		});
	}

	public synchronized long getPlayerTokens(OfflinePlayer p) {
		if (!p.isOnline()) {
			return this.plugin.getTokensManager().getPlayerTokens(p);
		} else {
			return tokensCache.getOrDefault(p.getUniqueId(), (long) 0);
		}
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

	public void removeTokens(OfflinePlayer p, long amount, CommandSender executor, LostCause cause) {
		long currentTokens = getPlayerTokens(p);
		long finalTokens = currentTokens - amount;

		if (finalTokens < 0) {
			finalTokens = 0;
		}

		this.callTokensLostEvent(cause, p, amount);

		if (!p.isOnline()) {
			this.plugin.getTokensService().setTokens(p, amount);
		} else {
			tokensCache.put(p.getUniqueId(), finalTokens);
		}
		if (executor != null) {
			PlayerUtils.sendMessage(executor, plugin.getTokensConfig().getMessage("admin_remove_tokens").replace("%player%", p.getName()).replace("%tokens%", String.format("%,d", amount)));
		}
	}

	private void callTokensLostEvent(LostCause cause, OfflinePlayer p, long amount) {
		PlayerTokensLostEvent event = new PlayerTokensLostEvent(cause, p, amount);
		Events.callSync(event);
	}

	private ItemStack createTokenItem(long amount, int value) {
		ItemStack item = ItemStackBuilder.of(
						this.plugin.getTokensConfig().getTokenItem().clone())
				.amount(value)
				.name(this.plugin.getTokensConfig().getTokenItemDisplayName().replace("%amount%", String.format("%,d", amount)).replace("%tokens%", String.format("%,d", amount)))
				.lore(this.plugin.getTokensConfig().getTokenItemLore())
				.enchant(Enchantment.PROTECTION_ENVIRONMENTAL)
				.flag(ItemFlag.HIDE_ENCHANTS)
				.build();
		final PrisonItem prisonItem = new PrisonItem(item);
		prisonItem.setTokens(amount);
		prisonItem.load();
		return item;
	}

	public void sendInfoMessage(CommandSender sender, OfflinePlayer target, boolean tokens) {
		Schedulers.async().run(() -> {
			if (sender == target) {
				if (tokens) {
					PlayerUtils.sendMessage(sender, plugin.getTokensConfig().getMessage("your_tokens").replace("%tokens%", String.format("%,d", this.getPlayerTokens(target))));
				} else {
					PlayerUtils.sendMessage(sender, plugin.getTokensConfig().getMessage("your_blocks").replace("%blocks%", String.format("%,d", this.getPlayerBrokenBlocks(target))));
				}
			} else {
				if (tokens) {
					PlayerUtils.sendMessage(sender, plugin.getTokensConfig().getMessage("other_tokens").replace("%tokens%", String.format("%,d", this.getPlayerTokens(target))).replace("%player%", target.getName()));
				} else {
					PlayerUtils.sendMessage(sender, plugin.getTokensConfig().getMessage("other_blocks").replace("%blocks%", String.format("%,d", this.getPlayerBrokenBlocks(target))).replace("%player%", target.getName()));
				}
			}
		});
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
				PlayerUtils.sendMessage(sender, plugin.getTokensConfig().getMessage("admin_give_blocks").replace("%player%", player.getName()).replace("%blocks%", String.format("%,d", finalAmount)));
			}
		});
	}

	public void addBlocksBroken(OfflinePlayer player, List<Block> blocks) {

		if (player.isOnline()) {
			XPrisonBlockBreakEvent event = new XPrisonBlockBreakEvent((Player) player, blocks);

			Events.call(event);

			if (event.isCancelled()) {
				return;
			}

			blocks = event.getBlocks();
		}

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

			PlayerUtils.sendMessage(sender, plugin.getTokensConfig().getMessage("admin_remove_blocks").replace("%player%", player.getName()).replace("%blocks%", String.format("%,d", amount)));

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

			PlayerUtils.sendMessage(sender, plugin.getTokensConfig().getMessage("admin_set_blocks").replace("%player%", player.getName()).replace("%blocks%", String.format("%,d", amount)));
		});
	}

	public void sendTokensTop(CommandSender sender) {

		List<String> format = this.plugin.getTokensConfig().getTokensTopFormat();

		Schedulers.async().run(() -> {
			Map<UUID, Long> topTokens = this.plugin.getTokensService().getTopTokens(this.plugin.getTokensConfig().getTopPlayersAmount());
			for (String s : format) {
				if (s.startsWith("{FOR_EACH_PLAYER}")) {
					String rawContent = s.replace("{FOR_EACH_PLAYER} ", "");
					for (int i = 0; i < 10; i++) {
						try {
							UUID uuid = (UUID) topTokens.keySet().toArray()[i];
							OfflinePlayer player = Players.getOfflineNullable(uuid);
							String name;
							if (player.getName() == null) {
								name = "Unknown Player";
							} else {
								name = player.getName();
							}
							long tokens = topTokens.get(uuid);
							PlayerUtils.sendMessage(sender, rawContent.replace("%position%", String.valueOf(i + 1)).replace("%player%", name).replace("%tokens%", String.format("%,d", tokens)));
						} catch (Exception e) {
							break;
						}
					}
				} else {
					PlayerUtils.sendMessage(sender, s);
				}
			}
		});
	}

	public void sendBlocksTop(CommandSender sender) {
		List<String> format = this.plugin.getTokensConfig().getBlocksTopFormat();
		Schedulers.async().run(() -> {
			Map<UUID, Long> topBlocks = this.plugin.getBlocksService().getTopBlocks(this.plugin.getTokensConfig().getTopPlayersAmount());
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
			} catch (Exception e) {
				break;
			}
		}
	}

	public void sendBlocksTopWeekly(CommandSender sender) {
		List<String> format = this.plugin.getTokensConfig().getBlocksTopFormatWeekly();

		Schedulers.async().run(() -> {
			Map<UUID, Long> topBlocksWeekly = this.plugin.getBlocksService().getTopBlocksWeekly(this.plugin.getTokensConfig().getTopPlayersAmount());
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
		this.plugin.getTokensConfig().setNextResetWeekly(Time.nowMillis() + TimeUnit.DAYS.toMillis(7));
		this.plugin.getBlocksService().resetBlocksWeekly();
		PlayerUtils.sendMessage(sender, "&aBlocksTop - Weekly - Reset!");
	}

	private void saveWeeklyReset() {
		this.plugin.getTokensConfig().getYamlConfig().set("next-reset-weekly", this.plugin.getTokensConfig().getNextResetWeekly());
		this.plugin.getTokensConfig().save();
	}

	public void toggleTokenMessage(Player p) {
		if (this.tokenMessageOnPlayers.contains(p.getUniqueId())) {
			PlayerUtils.sendMessage(p, plugin.getTokensConfig().getMessage("token_message_disabled"));
			this.tokenMessageOnPlayers.remove(p.getUniqueId());
		} else {
			PlayerUtils.sendMessage(p, plugin.getTokensConfig().getMessage("token_message_enabled"));
			this.tokenMessageOnPlayers.add(p.getUniqueId());
		}
	}

	public void reload() {

	}

	public void addPlayerIntoTokenMessageOnPlayers(Player player) {
		this.tokenMessageOnPlayers.add(player.getUniqueId());
	}

	public boolean hasOffTokenMessages(Player p) {
		return !this.tokenMessageOnPlayers.contains(p.getUniqueId());
	}

	public void disable() {
		this.saveWeeklyReset();
		this.savePlayerDataOnDisable();
	}


	public void handleBlockBreak(Player p, List<Block> blocks, boolean countBlocksBroken) {

		long startTime = System.currentTimeMillis();

		if (countBlocksBroken) {
			this.addBlocksBroken(p, blocks);
		}

		//Lucky block check
		blocks.forEach(block -> {
			List<String> rewards = this.plugin.getTokensConfig().getLuckyBlockReward(block.getType());
			for (String s : rewards) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName()));
			}
		});

		long totalAmount = 0;
		for (int i = 0; i < blocks.size(); i++) {
			double random = ThreadLocalRandom.current().nextDouble(100);

			if (this.plugin.getTokensConfig().getChance() >= random) {
				long randAmount = this.plugin.getTokensConfig().getMinAmount() == this.plugin.getTokensConfig().getMaxAmount() ? this.plugin.getTokensConfig().getMinAmount() : ThreadLocalRandom.current().nextLong(this.plugin.getTokensConfig().getMinAmount(), this.plugin.getTokensConfig().getMaxAmount());
				totalAmount += randAmount;
			}
		}
		if (totalAmount > 0) {
			this.giveTokens(p, totalAmount, null, ReceiveCause.MINING);
		}
		this.plugin.getCore().debug("XPrisonTokens::handleBlockBreak >> Took " + (System.currentTimeMillis() - startTime) + " ms.", this.plugin);
	}

	public void enable() {
		if (this.checkBlocksTopWeeklyReset()) {
			resetBlocksTopWeekly(Bukkit.getConsoleSender());
		}
		this.loadPlayerDataOnEnable();
	}

	private boolean checkBlocksTopWeeklyReset() {
		long nextResetWeeklyMillis = this.plugin.getTokensConfig().getNextResetWeekly();
		return Time.nowMillis() >= nextResetWeeklyMillis;
	}
}
