package dev.drawethree.ultraprisoncore.tokens.managers;

import de.tr7zw.changeme.nbtapi.NBTItem;
import dev.drawethree.ultraprisoncore.api.enums.LostCause;
import dev.drawethree.ultraprisoncore.api.enums.ReceiveCause;
import dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import dev.drawethree.ultraprisoncore.tokens.api.events.PlayerTokensLostEvent;
import dev.drawethree.ultraprisoncore.tokens.api.events.PlayerTokensReceiveEvent;
import dev.drawethree.ultraprisoncore.tokens.api.events.UltraPrisonBlockBreakEvent;
import dev.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import dev.drawethree.ultraprisoncore.utils.item.ItemStackBuilder;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import dev.drawethree.ultraprisoncore.utils.text.TextUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.time.Time;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TokensManager {


	private UltraPrisonTokens plugin;

	private List<String> tokensTopFormat;
	private List<String> blocksTopFormat;
	private List<String> blocksTopFormatWeekly;
	private HashMap<UUID, Long> tokensCache = new HashMap<>();
	private HashMap<UUID, Long> blocksCache = new HashMap<>();
	private HashMap<UUID, Long> blocksCacheWeekly = new HashMap<>();

	private LinkedHashMap<UUID, Long> top10Tokens = new LinkedHashMap<>();
	private LinkedHashMap<UUID, Long> top10Blocks = new LinkedHashMap<>();
	private LinkedHashMap<UUID, Long> top10BlocksWeekly = new LinkedHashMap<>();

	private LinkedHashMap<Long, BlockReward> blockRewards = new LinkedHashMap<>();

	private int topUpdateInterval;
	private Task task;

	private boolean updating;
	private boolean displayTokenMessages;
	private long nextResetWeekly;

	private String tokenItemDisplayName;
	private ItemStack tokenItem;
	private List<String> tokenItemLore;

	private List<UUID> tokenMessageOnPlayers;

	public TokensManager(UltraPrisonTokens plugin) {
		this.plugin = plugin;
		this.tokensTopFormat = this.plugin.getConfig().get().getStringList("tokens-top-format");
		this.blocksTopFormat = this.plugin.getConfig().get().getStringList("blocks-top-format");
		this.blocksTopFormatWeekly = this.plugin.getConfig().get().getStringList("blocks-top-weekly-format");
		this.nextResetWeekly = plugin.getConfig().get().getLong("next-reset-weekly");
		this.displayTokenMessages = plugin.getConfig().get().getBoolean("display-token-messages");
		this.topUpdateInterval = plugin.getConfig().get().getInt("top_update_interval");
		this.tokenItemDisplayName = plugin.getConfig().get().getString("tokens.item.name");
		this.tokenItemLore = plugin.getConfig().get().getStringList("tokens.item.lore");
		this.tokenItem = CompMaterial.fromString(plugin.getConfig().get().getString("tokens.item.material")).toItem();
		this.tokenMessageOnPlayers = new ArrayList<>();

		Events.subscribe(PlayerJoinEvent.class)
				.handler(e -> {
					this.addIntoTable(e.getPlayer());
					this.loadPlayerData(e.getPlayer());

					if (this.displayTokenMessages && hasOffTokenMessages(e.getPlayer())) {
						this.tokenMessageOnPlayers.add(e.getPlayer().getUniqueId());
					}

				}).bindWith(plugin.getCore());
		Events.subscribe(PlayerQuitEvent.class)
				.handler(e -> {
					this.savePlayerData(e.getPlayer(), true, true);
					e.getPlayer().getActivePotionEffects().forEach(effect -> e.getPlayer().removePotionEffect(effect.getType()));
				}).bindWith(plugin.getCore());

		this.loadPlayerDataOnEnable();
		this.loadBlockRewards();
		this.updateTop10();
	}

	private void loadBlockRewards() {
		this.blockRewards = new LinkedHashMap<>();
		for (String key : this.plugin.getBlockRewardsConfig().get().getConfigurationSection("block-rewards").getKeys(false)) {
			long blocksNeeded = Long.parseLong(key);
			String message = TextUtils.applyColor(this.plugin.getBlockRewardsConfig().get().getString("block-rewards." + key + ".message"));
			List<String> commands = this.plugin.getBlockRewardsConfig().get().getStringList("block-rewards." + key + ".commands");
			this.blockRewards.put(blocksNeeded, new BlockReward(blocksNeeded, commands, message));
		}
		this.plugin.getCore().getLogger().info("Loaded " + this.blockRewards.keySet().size() + " Block Rewards!");
	}

	public void stopUpdating() {
		this.plugin.getCore().getLogger().info("Stopping updating Top 10");
		task.close();
	}

	private void updateTop10() {
		this.updating = true;
		task = Schedulers.async().runRepeating(() -> {
			this.updating = true;
			Players.all().forEach(p -> savePlayerData(p, false, false));
			this.updateBlocksTop();
			this.updateBlocksTopWeekly();
			this.updateTokensTop();
			this.updating = false;
		}, 30, TimeUnit.SECONDS, this.topUpdateInterval, TimeUnit.MINUTES);
	}

	private void savePlayerData(Player player, boolean removeFromCache, boolean async) {
		if (async) {
			Schedulers.async().run(() -> {
				this.plugin.getCore().getPluginDatabase().updateTokens(player, tokensCache.getOrDefault(player.getUniqueId(), 0L));
				this.plugin.getCore().getPluginDatabase().updateBlocks(player, blocksCache.getOrDefault(player.getUniqueId(), 0L));
				this.plugin.getCore().getPluginDatabase().updateBlocksWeekly(player, blocksCacheWeekly.getOrDefault(player.getUniqueId(), 0L));
				if (removeFromCache) {
					tokensCache.remove(player.getUniqueId());
					blocksCache.remove(player.getUniqueId());
					blocksCacheWeekly.remove(player.getUniqueId());
				}
				this.plugin.getCore().getLogger().info(String.format("Saved player %s tokens & blocks broken to database.", player.getName()));
			});
		} else {
			this.plugin.getCore().getPluginDatabase().updateTokens(player, tokensCache.getOrDefault(player.getUniqueId(), 0L));
			this.plugin.getCore().getPluginDatabase().updateBlocks(player, blocksCache.getOrDefault(player.getUniqueId(), 0L));
			this.plugin.getCore().getPluginDatabase().updateBlocksWeekly(player, blocksCacheWeekly.getOrDefault(player.getUniqueId(), 0L));
			if (removeFromCache) {
				tokensCache.remove(player.getUniqueId());
				blocksCache.remove(player.getUniqueId());
				blocksCacheWeekly.remove(player.getUniqueId());
			}
			this.plugin.getCore().getLogger().info(String.format("Saved player %s tokens & blocks broken to database.", player.getName()));
		}
	}

	public void savePlayerDataOnDisable() {
		this.plugin.getCore().getLogger().info("[PLUGIN DISABLE] Saving all player data");
		Schedulers.sync().run(() -> {
			for (UUID uuid : blocksCache.keySet()) {
				this.plugin.getCore().getPluginDatabase().updateBlocks(Players.getOfflineNullable(uuid), blocksCache.get(uuid));
			}
			for (UUID uuid : tokensCache.keySet()) {
				this.plugin.getCore().getPluginDatabase().updateTokens(Players.getOfflineNullable(uuid), tokensCache.get(uuid));
			}
			for (UUID uuid : blocksCache.keySet()) {
				this.plugin.getCore().getPluginDatabase().updateBlocksWeekly(Players.getOfflineNullable(uuid), blocksCacheWeekly.get(uuid));
			}
			tokensCache.clear();
			blocksCache.clear();
			blocksCacheWeekly.clear();
			this.plugin.getCore().getLogger().info("[PLUGIN DISABLE] Saved all player data to database");
		});
	}

	private void addIntoTable(Player player) {
		Schedulers.async().run(() -> {
			this.plugin.getCore().getPluginDatabase().addIntoTokens(player);
			this.plugin.getCore().getPluginDatabase().addIntoBlocks(player);
			this.plugin.getCore().getPluginDatabase().addIntoBlocksWeekly(player);
		});
	}

	private void loadPlayerDataOnEnable() {
		Players.all().forEach(p -> loadPlayerData(p));
	}

	private void loadPlayerData(Player player) {
		Schedulers.async().run(() -> {

			long playerTokens = this.plugin.getCore().getPluginDatabase().getPlayerTokens(player);
			long playerBlocks = this.plugin.getCore().getPluginDatabase().getPlayerBrokenBlocks(player);
			long playerBlocksWeekly = this.plugin.getCore().getPluginDatabase().getPlayerBrokenBlocksWeekly(player);

			this.tokensCache.put(player.getUniqueId(), playerTokens);
			this.blocksCache.put(player.getUniqueId(), playerBlocks);
			this.blocksCacheWeekly.put(player.getUniqueId(), playerBlocksWeekly);

			this.plugin.getCore().getLogger().info(String.format("Loaded tokens and blocks broken of player %s from database", player.getName()));

		});
	}

	public void setTokens(OfflinePlayer p, long newAmount, CommandSender executor) {
		Schedulers.async().run(() -> {
			if (!p.isOnline()) {
				this.plugin.getCore().getPluginDatabase().updateTokens(p, newAmount);
			} else {
				tokensCache.put(p.getUniqueId(), newAmount);
			}
			PlayerUtils.sendMessage(executor, plugin.getMessage("admin_set_tokens").replace("%player%", p.getName()).replace("%tokens%", String.format("%,d", newAmount)));
		});
	}

	public void giveTokens(OfflinePlayer p, long amount, CommandSender executor, ReceiveCause cause) {
		long currentTokens = getPlayerTokens(p);

		this.plugin.getCore().debug("UltraPrisonPlayerTokenReceiveEvent :: Player Tokens :: " + currentTokens, this.plugin);

		long finalAmount = this.callTokensReceiveEvent(cause, p, amount);

		this.plugin.getCore().debug("UltraPrisonPlayerTokenReceiveEvent :: Final amount :: " + finalAmount, this.plugin);

		if (!p.isOnline()) {
			this.plugin.getCore().getPluginDatabase().updateTokens(p, finalAmount + currentTokens);
		} else {
			tokensCache.put(p.getUniqueId(), tokensCache.getOrDefault(p.getUniqueId(), (long) 0) + finalAmount);
			if (executor != null && executor instanceof ConsoleCommandSender && !this.hasOffTokenMessages(p.getPlayer())) {
				PlayerUtils.sendMessage(p.getPlayer(), plugin.getMessage("tokens_received_console").replace("%tokens%", String.format("%,d", finalAmount)).replace("%player%", executor == null ? "Console" : executor.getName()));
			} else if (cause == ReceiveCause.MINING && !this.hasOffTokenMessages(p.getPlayer())) {
				PlayerUtils.sendMessage(p.getPlayer(), this.plugin.getMessage("tokens_received_mining").replace("%amount%", String.format("%,d", finalAmount)));
			} else if (cause == ReceiveCause.LUCKY_BLOCK && !this.hasOffTokenMessages(p.getPlayer())) {
				PlayerUtils.sendMessage(p.getPlayer(), this.plugin.getMessage("lucky_block_mined").replace("%amount%", String.format("%,d", finalAmount)));
			}
		}

		this.plugin.getCore().debug("UltraPrisonPlayerTokenReceiveEvent :: Player tokens final  :: " + this.tokensCache.getOrDefault(p.getUniqueId(), 0L), this.plugin);

		if (executor != null && !(executor instanceof ConsoleCommandSender)) {
			PlayerUtils.sendMessage(executor, plugin.getMessage("admin_give_tokens").replace("%player%", p.getName()).replace("%tokens%", String.format("%,d", finalAmount)));
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
		NBTItem nbtItem = new NBTItem(item);
		if (nbtItem.hasKey("token-amount")) {
			long tokenAmount = nbtItem.getLong("token-amount");
			int itemAmount = item.getAmount();
			if (shiftClick) {
				if (offhand) {
					p.getInventory().setItemInOffHand(null);
				} else {
					p.setItemInHand(null);
				}
				this.giveTokens(p, tokenAmount * itemAmount, null, ReceiveCause.REDEEM);
				PlayerUtils.sendMessage(p, plugin.getMessage("tokens_redeem").replace("%tokens%", String.format("%,d", tokenAmount * itemAmount)));
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
				PlayerUtils.sendMessage(p, plugin.getMessage("tokens_redeem").replace("%tokens%", String.format("%,d", tokenAmount)));
			}
		} else {
			PlayerUtils.sendMessage(p, plugin.getMessage("not_token_item"));
		}
	}

	public void payTokens(Player executor, long amount, OfflinePlayer target) {
		Schedulers.async().run(() -> {
			if (getPlayerTokens(executor) >= amount) {
				this.removeTokens(executor, amount, null, LostCause.PAY);
				this.giveTokens(target, amount, null, ReceiveCause.PAY);
				PlayerUtils.sendMessage(executor, plugin.getMessage("tokens_send").replace("%player%", target.getName()).replace("%tokens%", String.format("%,d", amount)));
				if (target.isOnline()) {
					PlayerUtils.sendMessage((CommandSender) target, plugin.getMessage("tokens_received").replace("%player%", executor.getName()).replace("%tokens%", String.format("%,d", amount)));
				}
			} else {
				PlayerUtils.sendMessage(executor, plugin.getMessage("not_enough_tokens"));
			}
		});
	}

	public void withdrawTokens(Player executor, long amount, int value) {
		Schedulers.async().run(() -> {
			long totalAmount = amount * value;

			if (this.getPlayerTokens(executor) < totalAmount) {
				PlayerUtils.sendMessage(executor, plugin.getMessage("not_enough_tokens"));
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

			PlayerUtils.sendMessage(executor, plugin.getMessage("withdraw_successful").replace("%amount%", String.format("%,d", amount)).replace("%value%", String.format("%,d", value)));
		});
	}

	public synchronized long getPlayerTokens(OfflinePlayer p) {
		if (!p.isOnline()) {
			return this.plugin.getCore().getPluginDatabase().getPlayerTokens(p);
		} else {
			return tokensCache.getOrDefault(p.getUniqueId(), (long) 0);
		}
	}

	public synchronized long getPlayerBrokenBlocks(OfflinePlayer p) {
		if (!p.isOnline()) {
			return this.plugin.getCore().getPluginDatabase().getPlayerBrokenBlocks(p);
		} else {
			return blocksCache.getOrDefault(p.getUniqueId(), (long) 0);
		}
	}

	public synchronized long getPlayerBrokenBlocksWeekly(OfflinePlayer p) {
		if (!p.isOnline()) {
			return this.plugin.getCore().getPluginDatabase().getPlayerBrokenBlocksWeekly(p);
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
			this.plugin.getCore().getPluginDatabase().updateTokens(p, amount);
		} else {
			tokensCache.put(p.getUniqueId(), finalTokens);
		}
		if (executor != null) {
			PlayerUtils.sendMessage(executor, plugin.getMessage("admin_remove_tokens").replace("%player%", p.getName()).replace("%tokens%", String.format("%,d", amount)));
		}
	}

	private void callTokensLostEvent(LostCause cause, OfflinePlayer p, long amount) {
		PlayerTokensLostEvent event = new PlayerTokensLostEvent(cause, p, amount);
		Events.callSync(event);
	}

	private ItemStack createTokenItem(long amount, int value) {
		ItemStack item = ItemStackBuilder.of(this.tokenItem.clone()).amount(value).name(this.tokenItemDisplayName.replace("%amount%", String.format("%,d", amount)).replace("%tokens%", String.format("%,d", amount))).lore(this.tokenItemLore).enchant(Enchantment.PROTECTION_ENVIRONMENTAL).flag(ItemFlag.HIDE_ENCHANTS).build();
		NBTItem nbt = new NBTItem(item);
		nbt.setLong("token-amount", amount);
		return nbt.getItem();
	}

	public void sendInfoMessage(CommandSender sender, OfflinePlayer target, boolean tokens) {
		Schedulers.async().run(() -> {
			if (sender == target) {
				if (tokens) {
					PlayerUtils.sendMessage(sender, plugin.getMessage("your_tokens").replace("%tokens%", String.format("%,d", this.getPlayerTokens(target))));
				} else {
					PlayerUtils.sendMessage(sender, plugin.getMessage("your_blocks").replace("%blocks%", String.format("%,d", this.getPlayerBrokenBlocks(target))));
				}
			} else {
				if (tokens) {
					PlayerUtils.sendMessage(sender, plugin.getMessage("other_tokens").replace("%tokens%", String.format("%,d", this.getPlayerTokens(target))).replace("%player%", target.getName()));
				} else {
					PlayerUtils.sendMessage(sender, plugin.getMessage("other_blocks").replace("%blocks%", String.format("%,d", this.getPlayerBrokenBlocks(target))).replace("%player%", target.getName()));
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
				this.plugin.getCore().getPluginDatabase().updateBlocks(player, currentBroken + finalAmount);
				this.plugin.getCore().getPluginDatabase().updateBlocksWeekly(player, currentBrokenWeekly + finalAmount);
			} else {
				blocksCache.put(player.getUniqueId(), currentBroken + finalAmount);
				blocksCacheWeekly.put(player.getUniqueId(), currentBrokenWeekly + finalAmount);

				while (nextReward != null && nextReward.getBlocksRequired() <= blocksCache.get(player.getUniqueId())) {
					nextReward.giveTo((Player) player);
					nextReward = this.getNextBlockReward(nextReward);
				}
			}

			if (sender != null && !(sender instanceof ConsoleCommandSender)) {
				PlayerUtils.sendMessage(sender, plugin.getMessage("admin_give_blocks").replace("%player%", player.getName()).replace("%blocks%", String.format("%,d", finalAmount)));
			}
		});
	}

	public void addBlocksBroken(OfflinePlayer player, List<Block> blocks) {

		if (player.isOnline()) {
			UltraPrisonBlockBreakEvent event = new UltraPrisonBlockBreakEvent((Player) player, blocks);

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
				this.plugin.getCore().getPluginDatabase().updateBlocks(player, currentBroken + finalAmount);
				this.plugin.getCore().getPluginDatabase().updateBlocksWeekly(player, currentBrokenWeekly + finalAmount);
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
		for (long l : blockRewards.keySet()) {
			if (next) {
				return blockRewards.get(l);
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
				this.plugin.getCore().getPluginDatabase().updateBlocks(player, currentBroken - amount);
				this.plugin.getCore().getPluginDatabase().updateBlocksWeekly(player, currentBrokenWeekly - amount);
			} else {
				blocksCache.put(player.getUniqueId(), currentBroken - amount);
				blocksCacheWeekly.put(player.getUniqueId(), currentBrokenWeekly - amount);
			}

			PlayerUtils.sendMessage(sender, plugin.getMessage("admin_remove_blocks").replace("%player%", player.getName()).replace("%blocks%", String.format("%,d", amount)));

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
				this.plugin.getCore().getPluginDatabase().updateBlocks(player, amount);
				this.plugin.getCore().getPluginDatabase().updateBlocksWeekly(player, amount);
			} else {
				blocksCache.put(player.getUniqueId(), amount);
				blocksCacheWeekly.put(player.getUniqueId(), amount);

				while (nextReward != null && nextReward.getBlocksRequired() <= blocksCache.get(player.getUniqueId())) {
					nextReward.giveTo((Player) player);
					nextReward = this.getNextBlockReward(nextReward);
				}
			}

			PlayerUtils.sendMessage(sender, plugin.getMessage("admin_set_blocks").replace("%player%", player.getName()).replace("%blocks%", String.format("%,d", amount)));

		});
	}

	private void updateTokensTop() {
		top10Tokens = new LinkedHashMap<>();
		this.plugin.getCore().debug("Starting updating TokensTop", this.plugin);
		this.top10Tokens = (LinkedHashMap<UUID, Long>) this.plugin.getCore().getPluginDatabase().getTop10Tokens();
		this.plugin.getCore().debug("TokensTop updated!", this.plugin);
	}

	private void updateBlocksTop() {
		top10Blocks = new LinkedHashMap<>();
		this.plugin.getCore().debug("Starting updating BlocksTop", this.plugin);
		this.top10Blocks = (LinkedHashMap<UUID, Long>) this.plugin.getCore().getPluginDatabase().getTop10Blocks();
		this.plugin.getCore().debug("BlocksTop updated!", this.plugin);
	}

	private void updateBlocksTopWeekly() {
		top10BlocksWeekly = new LinkedHashMap<>();
		this.plugin.getCore().debug("Starting updating BlocksTop - Weekly", this.plugin);
		this.top10BlocksWeekly = (LinkedHashMap<UUID, Long>) this.plugin.getCore().getPluginDatabase().getTop10BlocksWeekly();
		this.plugin.getCore().debug("BlocksTop updated!", this.plugin);
	}

	public void sendTokensTop(CommandSender sender) {
		if (this.updating) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("top_updating"));
			return;
		}

		for (String s : this.tokensTopFormat) {
			if (s.startsWith("{FOR_EACH_PLAYER}")) {
				String rawContent = s.replace("{FOR_EACH_PLAYER} ", "");
				for (int i = 0; i < 10; i++) {
					try {
						UUID uuid = (UUID) top10Tokens.keySet().toArray()[i];
						OfflinePlayer player = Players.getOfflineNullable(uuid);
						String name;
						if (player.getName() == null) {
							name = "Unknown Player";
						} else {
							name = player.getName();
						}
						long tokens = top10Tokens.get(uuid);
						PlayerUtils.sendMessage(sender, rawContent.replace("%position%", String.valueOf(i + 1)).replace("%player%", name).replace("%tokens%", String.format("%,d", tokens)));
					} catch (Exception e) {
						break;
					}
				}
			} else {
				PlayerUtils.sendMessage(sender, s);
			}
		}
	}

	public void sendBlocksTop(CommandSender sender) {
		if (this.updating) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("top_updating"));
			return;
		}

		for (String s : this.blocksTopFormat) {
			if (s.startsWith("{FOR_EACH_PLAYER}")) {
				sendBlocksTop(sender, s, top10Blocks);
			} else {
				PlayerUtils.sendMessage(sender, s);
			}
		}
	}

	private void sendBlocksTop(CommandSender sender, String s, LinkedHashMap<UUID, Long> top) {
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
		if (this.updating) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("top_updating"));
			return;
		}

		for (String s : this.blocksTopFormatWeekly) {
			if (s.startsWith("{FOR_EACH_PLAYER}")) {
				sendBlocksTop(sender, s, top10BlocksWeekly);
			} else {
				PlayerUtils.sendMessage(sender, s);
			}
		}
	}


	public int getBlocksTopWeeklyPosition(Player p) {
		for (int i = 0; i < top10BlocksWeekly.keySet().size(); i++) {
			UUID uuid = (UUID) top10BlocksWeekly.keySet().toArray()[i];
			if (uuid.equals(p.getUniqueId())) {
				return i + 1;
			}
		}
		return -1;
	}

	public BlockReward getNextBlockReward(OfflinePlayer p) {
		long blocksBroken = this.getPlayerBrokenBlocks(p);

		for (long l : this.blockRewards.keySet()) {
			if (l > blocksBroken) {
				return this.blockRewards.get(l);
			}
		}
		return null;
	}

	public void resetBlocksTopWeekly(CommandSender sender) {
		Schedulers.async().run(() -> {
			PlayerUtils.sendMessage(sender, "&7&oStarting to reset BlocksTop - Weekly. This may take a while...");
			this.top10BlocksWeekly.clear();
			this.nextResetWeekly = Time.nowMillis() + TimeUnit.DAYS.toMillis(7);
			this.plugin.getCore().getPluginDatabase().resetBlocksWeekly(sender);
			PlayerUtils.sendMessage(sender, "&aBlocksTop - Weekly - Resetted!");
		});
	}

	private String getTimeLeftUntilWeeklyReset() {

		if (System.currentTimeMillis() > nextResetWeekly) {
			return "RESET SOON";
		}


		long timeLeft = nextResetWeekly - System.currentTimeMillis();

		long days = timeLeft / (24 * 60 * 60 * 1000);
		timeLeft -= days * (24 * 60 * 60 * 1000);

		long hours = timeLeft / (60 * 60 * 1000);
		timeLeft -= hours * (60 * 60 * 1000);

		long minutes = timeLeft / (60 * 1000);
		timeLeft -= minutes * (60 * 1000);

		long seconds = timeLeft / (1000);

		timeLeft -= seconds * 1000;

		return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
	}

	public void saveWeeklyReset() {
		this.plugin.getConfig().set("next-reset-weekly", this.nextResetWeekly).save();

	}

	public void reloadConfig() {
		this.tokensTopFormat = this.plugin.getConfig().get().getStringList("tokens-top-format");
		this.blocksTopFormat = this.plugin.getConfig().get().getStringList("blocks-top-format");
		this.blocksTopFormatWeekly = this.plugin.getConfig().get().getStringList("blocks-top-weekly-format");

		this.nextResetWeekly = plugin.getConfig().get().getLong("next-reset-weekly");
		this.displayTokenMessages = plugin.getConfig().get().getBoolean("display-token-messages");
		this.topUpdateInterval = plugin.getConfig().get().getInt("top_update_interval");
		this.tokenItemDisplayName = plugin.getConfig().get().getString("tokens.item.name");
		this.tokenItemLore = plugin.getConfig().get().getStringList("tokens.item.lore");
		this.tokenItem = CompMaterial.fromString(plugin.getConfig().get().getString("tokens.item.material")).toItem();
		this.loadBlockRewards();
	}

	public void toggleTokenMessage(Player p) {
		if (this.tokenMessageOnPlayers.contains(p.getUniqueId())) {
			PlayerUtils.sendMessage(p, plugin.getMessage("token_message_disabled"));
			this.tokenMessageOnPlayers.remove(p.getUniqueId());
		} else {
			PlayerUtils.sendMessage(p, plugin.getMessage("token_message_enabled"));
			this.tokenMessageOnPlayers.add(p.getUniqueId());
		}
	}

	public boolean hasOffTokenMessages(Player p) {
		return !this.tokenMessageOnPlayers.contains(p.getUniqueId());
	}

	public Material getTokenItemMaterial() {
		return this.tokenItem.getType();
	}

	@AllArgsConstructor
	@Getter
	private class BlockReward {

		private long blocksRequired;
		private List<String> commandsToRun;
		private String message;

		public void giveTo(Player p) {

			if (!Bukkit.isPrimaryThread()) {
				Schedulers.sync().run(() -> {
					for (String s : this.commandsToRun) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName()));
					}
				});
			} else {
				for (String s : this.commandsToRun) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName()));
				}
			}
			PlayerUtils.sendMessage(p, this.message);
		}


	}
}
