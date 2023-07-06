package dev.drawethree.xprison.gems.managers;

import dev.drawethree.xprison.api.enums.LostCause;
import dev.drawethree.xprison.api.enums.ReceiveCause;
import dev.drawethree.xprison.gems.XPrisonGems;
import dev.drawethree.xprison.gems.api.events.PlayerGemsLostEvent;
import dev.drawethree.xprison.gems.api.events.PlayerGemsReceiveEvent;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import dev.drawethree.xprison.utils.item.PrisonItem;
import dev.drawethree.xprison.utils.misc.NumberUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GemsManager {


	private final XPrisonGems plugin;
	private String SPACER_LINE;
	private String SPACER_LINE_BOTTOM;
	private String TOP_FORMAT_GEMS;
	private final Map<UUID, Long> gemsCache = new ConcurrentHashMap<>();
	private Map<UUID, Long> top10Gems = new LinkedHashMap<>();
	private Task task;
	private boolean updating;
	private boolean displayGemsMessages;

	private String gemsItemDisplayName;
	private ItemStack gemsItem;
	private List<String> gemsItemLore;
	private final List<UUID> gemsMessageOnPlayers;

	private long startingGems;

	public GemsManager(XPrisonGems plugin) {
		this.plugin = plugin;
		this.gemsMessageOnPlayers = new ArrayList<>();
		this.reload();

		Events.subscribe(PlayerJoinEvent.class)
				.handler(e -> {
					this.addIntoTable(e.getPlayer());
					this.loadPlayerData(e.getPlayer());
					if (this.displayGemsMessages && hasOffGemsMessages(e.getPlayer())) {
						this.gemsMessageOnPlayers.add(e.getPlayer().getUniqueId());
					}
				}).bindWith(plugin.getCore());
		Events.subscribe(PlayerQuitEvent.class)
				.handler(e -> {
					this.savePlayerData(e.getPlayer(), true, true);
					e.getPlayer().getActivePotionEffects().forEach(effect -> e.getPlayer().removePotionEffect(effect.getType()));
				}).bindWith(plugin.getCore());

		this.loadPlayerDataOnEnable();
		this.updateTop10();
	}

	public void reload() {
		this.SPACER_LINE = plugin.getMessage("top_spacer_line");
		this.SPACER_LINE_BOTTOM = plugin.getMessage("top_spacer_line_bottom");
		this.TOP_FORMAT_GEMS = plugin.getMessage("top_format_gems");
		this.displayGemsMessages = plugin.getConfig().get().getBoolean("display-gems-messages");
		this.gemsItemDisplayName = plugin.getConfig().get().getString("gems.item.name");
		this.gemsItemLore = plugin.getConfig().get().getStringList("gems.item.lore");
		this.gemsItem = CompMaterial.fromString(plugin.getConfig().get().getString("gems.item.material")).toItem();
		this.startingGems = plugin.getConfig().get().getLong("starting-gems");
	}

	public void stopUpdating() {
		this.plugin.getCore().debug("Stopping updating Top 10 - Gems", this.plugin);
		task.close();
	}

	private void updateTop10() {
		this.updating = true;
		task = Schedulers.async().runRepeating(() -> {
			this.updating = true;
			Players.all().forEach(p -> savePlayerData(p, false, false));
			this.updateGemsTop();
			this.updating = false;
		}, 1, TimeUnit.MINUTES, 1, TimeUnit.HOURS);
	}

	private void savePlayerData(Player player, boolean removeFromCache, boolean async) {
		if (async) {
			Schedulers.async().run(() -> {
				this.plugin.getGemsService().setGems(player, gemsCache.getOrDefault(player.getUniqueId(), 0L));
				if (removeFromCache) {
					gemsCache.remove(player.getUniqueId());
				}
				this.plugin.getCore().debug(String.format("Saved player %s gems to database.", player.getName()), this.plugin);
			});
		} else {
			this.plugin.getGemsService().setGems(player, gemsCache.getOrDefault(player.getUniqueId(), 0L));
			if (removeFromCache) {
				gemsCache.remove(player.getUniqueId());
			}
			this.plugin.getCore().debug(String.format("Saved player %s gems to database.", player.getName()), this.plugin);
		}
	}

	public void savePlayerDataOnDisable() {
		for (UUID uuid : gemsCache.keySet()) {
			this.plugin.getGemsService().setGems(Players.getOfflineNullable(uuid), gemsCache.getOrDefault(uuid, 0L));
		}
		gemsCache.clear();
		this.plugin.getCore().getLogger().info("Saved online players gems.");
	}

	private void addIntoTable(Player player) {
		Schedulers.async().run(() -> {
			this.plugin.getGemsService().createGems(player, startingGems);
		});
	}

	private void loadPlayerDataOnEnable() {
		Players.all().forEach(p -> loadPlayerData(p));
	}

	private void loadPlayerData(Player player) {
		Schedulers.async().run(() -> {
			long playerGems = this.plugin.getGemsService().getPlayerGems(player);
			this.gemsCache.put(player.getUniqueId(), playerGems);
			this.plugin.getCore().debug(String.format("Loaded gems of player %s from database", player.getName()), this.plugin);
		});
	}

	public void setGems(OfflinePlayer p, long newAmount, CommandSender executor) {
		Schedulers.async().run(() -> {
			if (!p.isOnline()) {
				this.plugin.getGemsService().setGems(p, newAmount);
			} else {
				gemsCache.put(p.getUniqueId(), newAmount);
			}
			PlayerUtils.sendMessage(executor, plugin.getMessage("admin_set_gems").replace("%player%", p.getName()).replace("%gems%", String.format("%,d", newAmount)));
		});
	}

	public void giveGems(OfflinePlayer p, long amount, CommandSender executor, ReceiveCause cause) {
		long currentGems = getPlayerGems(p);

		this.plugin.getCore().debug("XPrisonPlayerGemsReceiveEvent :: Player Gems :: " + currentGems, this.plugin);

		long finalAmount = this.callGemsReceiveEvent(cause, p, amount);

		this.plugin.getCore().debug("XPrisonPlayerGemsReceiveEvent :: Final amount :: " + finalAmount, this.plugin);

		long newAmount;

		if (NumberUtils.wouldAdditionBeOverMaxLong(currentGems, finalAmount)) {
			newAmount = Long.MAX_VALUE;
		} else {
			newAmount = currentGems + finalAmount;
		}

		if (!p.isOnline()) {
			this.plugin.getGemsService().setGems(p, newAmount);
		} else {
			gemsCache.put(p.getUniqueId(), newAmount);
			if (executor instanceof ConsoleCommandSender && !this.hasOffGemsMessages(p.getPlayer())) {
				PlayerUtils.sendMessage(p.getPlayer(), plugin.getMessage("gems_received_console").replace("%gems%", String.format("%,d", finalAmount)).replace("%player%", executor == null ? "Console" : executor.getName()));
			} else if (cause == ReceiveCause.MINING && !this.hasOffGemsMessages(p.getPlayer())) {
				PlayerUtils.sendMessage(p.getPlayer(), this.plugin.getMessage("gems_received_mining").replace("%amount%", String.format("%,d", finalAmount)));
			}
		}

		this.plugin.getCore().debug("XPrisonPlayerGemsReceiveEvent :: Player gems final  :: " + this.gemsCache.getOrDefault(p.getUniqueId(), 0L), this.plugin);

		if (executor != null && !(executor instanceof ConsoleCommandSender)) {
			PlayerUtils.sendMessage(executor, plugin.getMessage("admin_give_gems").replace("%player%", p.getName()).replace("%gems%", String.format("%,d", finalAmount)));
		}

	}

	private long callGemsReceiveEvent(ReceiveCause cause, OfflinePlayer p, long amount) {
		PlayerGemsReceiveEvent event = new PlayerGemsReceiveEvent(cause, p, amount);

		Events.call(event);

		if (event.isCancelled()) {
			return amount;
		}

		return event.getAmount();
	}

	public void redeemGems(Player p, ItemStack item, boolean shiftClick, boolean offhand) {
		final Long gemsAmount = new PrisonItem(item).getGems();
		if (gemsAmount == null) {
			PlayerUtils.sendMessage(p, plugin.getMessage("not_gems_item"));
			return;
		}
		int itemAmount = item.getAmount();
		if (shiftClick) {
			if (offhand) {
				p.getInventory().setItemInOffHand(null);
			} else {
				p.setItemInHand(null);
			}
			this.giveGems(p, gemsAmount * itemAmount, null, ReceiveCause.REDEEM);
			PlayerUtils.sendMessage(p, plugin.getMessage("gems_redeem").replace("%gems%", String.format("%,d", gemsAmount * itemAmount)));
		} else {
			this.giveGems(p, gemsAmount, null, ReceiveCause.REDEEM);
			if (item.getAmount() == 1) {
				if (offhand) {
					p.getInventory().setItemInOffHand(null);
				} else {
					p.setItemInHand(null);
				}
			} else {
				item.setAmount(item.getAmount() - 1);
			}
			PlayerUtils.sendMessage(p, plugin.getMessage("gems_redeem").replace("%gems%", String.format("%,d", gemsAmount)));
		}
	}

	public void payGems(Player executor, long amount, OfflinePlayer target) {
		Schedulers.async().run(() -> {
			if (getPlayerGems(executor) >= amount) {
				this.removeGems(executor, amount, null, LostCause.PAY);
				this.giveGems(target, amount, null, ReceiveCause.PAY);
				PlayerUtils.sendMessage(executor, plugin.getMessage("gems_send").replace("%player%", target.getName()).replace("%gems%", String.format("%,d", amount)));
				if (target.isOnline()) {
					PlayerUtils.sendMessage((CommandSender) target, plugin.getMessage("gems_received").replace("%player%", executor.getName()).replace("%gems%", String.format("%,d", amount)));
				}
			} else {
				PlayerUtils.sendMessage(executor, plugin.getMessage("not_enough_gems"));
			}
		});
	}

	public void withdrawGems(Player executor, long amount, int value) {
		Schedulers.async().run(() -> {
			long totalAmount = amount * value;

			if (this.getPlayerGems(executor) < totalAmount) {
				PlayerUtils.sendMessage(executor, plugin.getMessage("not_enough_gems"));
				return;
			}

			removeGems(executor, totalAmount, null, LostCause.WITHDRAW);

			ItemStack item = createGemsItem(amount, value);
			Collection<ItemStack> notFit = executor.getInventory().addItem(item).values();

			if (!notFit.isEmpty()) {
				notFit.forEach(itemStack -> {
					this.giveGems(executor, amount * item.getAmount(), null, ReceiveCause.REDEEM);
				});
			}

			PlayerUtils.sendMessage(executor, plugin.getMessage("withdraw_successful").replace("%amount%", String.format("%,d", amount)).replace("%value%", String.format("%,d", value)));
		});
	}

	public synchronized long getPlayerGems(OfflinePlayer p) {
		if (!p.isOnline()) {
			return this.plugin.getGemsService().getPlayerGems(p);
		} else {
			return gemsCache.getOrDefault(p.getUniqueId(), (long) 0);
		}
	}

	public void removeGems(OfflinePlayer p, long amount, CommandSender executor, LostCause cause) {
		Schedulers.async().run(() -> {
			long currentgems = getPlayerGems(p);
			long finalgems = currentgems - amount;

			if (finalgems < 0) {
				finalgems = 0;
			}

			this.callGemsLostEvent(cause, p, amount);

			if (!p.isOnline()) {
				this.plugin.getGemsService().setGems(p, finalgems);
			} else {
				gemsCache.put(p.getUniqueId(), finalgems);
			}
			if (executor != null) {
				PlayerUtils.sendMessage(executor, plugin.getMessage("admin_remove_gems").replace("%player%", p.getName()).replace("%gems%", String.format("%,d", amount)));
			}
		});
	}

	private void callGemsLostEvent(LostCause cause, OfflinePlayer p, long amount) {
		PlayerGemsLostEvent event = new PlayerGemsLostEvent(cause, p, amount);
		Events.callSync(event);
	}

	private ItemStack createGemsItem(long amount, int value) {
		ItemStack item = ItemStackBuilder.of(this.gemsItem.clone()).amount(value).name(this.gemsItemDisplayName.replace("%amount%", String.format("%,d", amount)).replace("%tokens%", String.format("%,d", amount))).lore(this.gemsItemLore).enchant(Enchantment.PROTECTION_ENVIRONMENTAL).flag(ItemFlag.HIDE_ENCHANTS).build();
		final PrisonItem prisonItem = new PrisonItem(item);
		prisonItem.setGems(amount);
		prisonItem.load();
		return item;
	}

	public void sendInfoMessage(CommandSender sender, OfflinePlayer target) {
		Schedulers.async().run(() -> {
			if (sender == target) {
				PlayerUtils.sendMessage(sender, plugin.getMessage("your_gems").replace("%gems%", String.format("%,d", this.getPlayerGems(target))));
			} else {
				PlayerUtils.sendMessage(sender, plugin.getMessage("other_gems").replace("%gems%", String.format("%,d", this.getPlayerGems(target))).replace("%player%", target.getName()));
			}
		});
	}


	private void updateGemsTop() {
		top10Gems = new LinkedHashMap<>();
		this.plugin.getCore().debug("Starting updating Top 10 - Gems", this.plugin);
		this.top10Gems = this.plugin.getGemsService().getTopGems(10);
		this.plugin.getCore().debug("GemsTop updated!", this.plugin);
	}

	public void sendGemsTop(CommandSender sender) {
		Schedulers.async().run(() -> {
			PlayerUtils.sendMessage(sender, SPACER_LINE);
			if (this.updating) {
				PlayerUtils.sendMessage(sender, this.plugin.getMessage("top_updating"));
				PlayerUtils.sendMessage(sender, SPACER_LINE_BOTTOM);
				return;
			}
			for (int i = 0; i < 10; i++) {
				try {
					UUID uuid = (UUID) top10Gems.keySet().toArray()[i];
					OfflinePlayer player = Players.getOfflineNullable(uuid);
					String name;
					if (player.getName() == null) {
						name = "Unknown Player";
					} else {
						name = player.getName();
					}
					long gems = top10Gems.get(uuid);
					PlayerUtils.sendMessage(sender, TOP_FORMAT_GEMS.replace("%position%", String.valueOf(i + 1)).replace("%player%", name).replace("%amount%", String.format("%,d", gems)));
				} catch (ArrayIndexOutOfBoundsException e) {
					break;
				}
			}
			PlayerUtils.sendMessage(sender, SPACER_LINE_BOTTOM);
		});
	}


	public void toggleGemsMessage(Player p) {
		if (this.gemsMessageOnPlayers.contains(p.getUniqueId())) {
			PlayerUtils.sendMessage(p, plugin.getMessage("gems_message_disabled"));
			this.gemsMessageOnPlayers.remove(p.getUniqueId());
		} else {
			PlayerUtils.sendMessage(p, plugin.getMessage("gems_message_enabled"));
			this.gemsMessageOnPlayers.add(p.getUniqueId());
		}
	}

	public boolean hasOffGemsMessages(Player p) {
		return !this.gemsMessageOnPlayers.contains(p.getUniqueId());
	}

	public Material getGemsItemMaterial() {
		return this.gemsItem.getType();
	}
}
