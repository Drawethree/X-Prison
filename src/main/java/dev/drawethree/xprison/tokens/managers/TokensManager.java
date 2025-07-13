package dev.drawethree.xprison.tokens.managers;

import com.cryptomorin.xseries.XEnchantment;
import dev.drawethree.xprison.shared.currency.enums.LostCause;
import dev.drawethree.xprison.shared.currency.enums.ReceiveCause;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import dev.drawethree.xprison.utils.item.PrisonToolItem;
import dev.drawethree.xprison.utils.misc.NumberUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.error;
import static dev.drawethree.xprison.utils.log.XPrisonLogger.info;

public class TokensManager {

	private final XPrisonTokens plugin;
	private final Map<UUID, Long> tokensCache;
	private final List<UUID> tokenMessageOnPlayers;

	public TokensManager(XPrisonTokens plugin) {
		this.plugin = plugin;
		this.tokenMessageOnPlayers = new ArrayList<>();
		this.tokensCache = new ConcurrentHashMap<>();
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

			if (removeFromCache) {
				this.tokensCache.remove(player.getUniqueId());
			}

			this.plugin.getCore().debug(String.format("Saved player %s tokens to database.", player.getName()), this.plugin);
		}
	}

	public void savePlayerDataOnDisable() {
		for (UUID uuid : tokensCache.keySet()) {
			this.plugin.getTokensService().setTokens(Players.getOfflineNullable(uuid), tokensCache.get(uuid));
		}
		tokensCache.clear();
		info("&aTokens saved.");

	}

	private void loadPlayerDataOnEnable() {
		loadPlayerData(Players.all());
	}

	public void loadPlayerData(Collection<Player> players) {
		Schedulers.async().run(() -> {
			for (Player player : players) {

				this.plugin.getTokensService().createTokens(player, this.plugin.getTokensConfig().getStartingTokens());

				long playerTokens = this.plugin.getTokensService().getTokens(player);

				this.tokensCache.put(player.getUniqueId(), playerTokens);

				this.plugin.getCore().debug(String.format("Loaded tokens of player %s from database", player.getName()), this.plugin);
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

		long newAmount;

		if (NumberUtils.wouldAdditionBeOverMaxLong(currentTokens, amount)) {
			newAmount = Long.MAX_VALUE;
		} else {
			newAmount = currentTokens + amount;
		}

		if (!p.isOnline()) {
			this.plugin.getTokensService().setTokens(p, newAmount);
		} else {
			tokensCache.put(p.getUniqueId(), newAmount);
			if (executor instanceof ConsoleCommandSender && !this.hasOffTokenMessages(p.getPlayer())) {
				PlayerUtils.sendMessage(p.getPlayer(), plugin.getTokensConfig().getMessage("tokens_received_console").replace("%tokens%", String.format("%,d", amount)).replace("%player%", executor == null ? "Console" : executor.getName()));
			} else if (cause == ReceiveCause.MINING && !this.hasOffTokenMessages(p.getPlayer())) {
				PlayerUtils.sendMessage(p.getPlayer(), this.plugin.getTokensConfig().getMessage("tokens_received_mining").replace("%amount%", String.format("%,d", amount)));
			} else if (cause == ReceiveCause.LUCKY_BLOCK && !this.hasOffTokenMessages(p.getPlayer())) {
				PlayerUtils.sendMessage(p.getPlayer(), this.plugin.getTokensConfig().getMessage("lucky_block_mined").replace("%amount%", String.format("%,d", amount)));
			}
		}

		this.plugin.getCore().debug("XPlayerTokenReceiveEvent :: Player tokens final  :: " + this.tokensCache.getOrDefault(p.getUniqueId(), 0L), this.plugin);

		if (executor != null && !(executor instanceof ConsoleCommandSender)) {
			PlayerUtils.sendMessage(executor, plugin.getTokensConfig().getMessage("admin_give_tokens").replace("%player%", p.getName()).replace("%tokens%", String.format("%,d", amount)));
		}
	}

	public void redeemTokens(Player p, ItemStack item, boolean shiftClick, boolean offhand) {
        final Long tokenAmount = new PrisonToolItem(item).getTokens();
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

	public boolean hasEnough(OfflinePlayer p, long amount) {
		return getPlayerTokens(p) >= amount;
	}

	public synchronized long getPlayerTokens(OfflinePlayer p) {
		if (!p.isOnline()) {
			return this.plugin.getTokensManager().getPlayerTokens(p);
		} else {
			return tokensCache.getOrDefault(p.getUniqueId(), (long) 0);
		}
	}

	public void removeTokens(OfflinePlayer p, long amount, CommandSender executor, LostCause cause) {
		long currentTokens = getPlayerTokens(p);
		long finalTokens = currentTokens - amount;

		if (finalTokens < 0) {
			finalTokens = 0;
		}

		if (!p.isOnline()) {
			this.plugin.getTokensService().setTokens(p, amount);
		} else {
			tokensCache.put(p.getUniqueId(), finalTokens);
		}
		if (executor != null) {
			PlayerUtils.sendMessage(executor, plugin.getTokensConfig().getMessage("admin_remove_tokens").replace("%player%", p.getName()).replace("%tokens%", String.format("%,d", amount)));
		}
	}

	private ItemStack createTokenItem(long amount, int value) {
		ItemStack item = ItemStackBuilder.of(
						this.plugin.getTokensConfig().getTokenItem().clone())
				.amount(value)
				.customModelData(this.plugin.getTokensConfig().getTokenItemCustomModelData())
				.name(this.plugin.getTokensConfig().getTokenItemDisplayName().replace("%amount%", String.format("%,d", amount)).replace("%tokens%", String.format("%,d", amount)))
				.lore(this.plugin.getTokensConfig().getTokenItemLore())
				.enchant(XEnchantment.PROTECTION.get())
				.flag(ItemFlag.HIDE_ENCHANTS)
				.build();
		final PrisonToolItem prisonToolItem = new PrisonToolItem(item);
		prisonToolItem.setTokens(amount);
		prisonToolItem.load();
		return item;
	}

	public void sendInfoMessage(CommandSender sender, OfflinePlayer target) {
		Schedulers.async().run(() -> {
			if (sender == target) {
				PlayerUtils.sendMessage(sender, plugin.getTokensConfig().getMessage("your_tokens").replace("%tokens%", String.format("%,d", this.getPlayerTokens(target))));
			} else {
				PlayerUtils.sendMessage(sender, plugin.getTokensConfig().getMessage("other_tokens").replace("%tokens%", String.format("%,d", this.getPlayerTokens(target))).replace("%player%", target.getName()));
			}
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
						} catch (IndexOutOfBoundsException e) {
							break;
						} catch (Exception e) {
							error("Exception during sending TokensTop to " + sender.getName());
							e.printStackTrace();
						}
					}
				} else {
					PlayerUtils.sendMessage(sender, s);
				}
			}
		});
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


	public void handleBlockBreak(Player p, List<Block> blocks) {

		long startTime = System.currentTimeMillis();

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
		this.loadPlayerDataOnEnable();
	}
}
