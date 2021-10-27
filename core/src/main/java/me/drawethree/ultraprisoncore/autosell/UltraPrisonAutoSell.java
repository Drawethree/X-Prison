package me.drawethree.ultraprisoncore.autosell;

import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.api.events.UltraPrisonAutoSellEvent;
import me.drawethree.ultraprisoncore.api.events.UltraPrisonSellAllEvent;
import me.drawethree.ultraprisoncore.autosell.api.UltraPrisonAutoSellAPI;
import me.drawethree.ultraprisoncore.autosell.api.UltraPrisonAutoSellAPIImpl;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.database.DatabaseType;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.implementations.LuckyBoosterEnchant;
import me.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import me.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
import me.drawethree.ultraprisoncore.utils.MaterialUtils;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.drawethree.ultraprisoncore.utils.RegionUtils;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class UltraPrisonAutoSell implements UltraPrisonModule {

	public static final String MODULE_NAME = "Auto Sell";

	private static final String ADMIN_PERMISSION = "ultraprison.autosell.admin";
	private static final String AUTOSELL_TOGGLE_PERMISSION = "ultraprison.autosell.toggle";

	@Getter
	private FileManager.Config config;

	private HashMap<String, SellRegion> regionsAutoSell;
	private HashMap<String, String> messages;
	private List<String> autoSellBroadcastMessage;
	private long broadcastTime;
	private HashMap<UUID, Double> lastEarnings;
	private HashMap<UUID, Long> lastItems;
	@Getter
	private UltraPrisonAutoSellAPI api;
	private List<UUID> enabledAutoSell;
	@Getter
	private UltraPrisonCore core;
	private boolean enabled;
	private boolean enableAutosellAutomatically;
	private boolean autoSmelt;

	private boolean inventoryFullNoticiation;
	private List<String> inventoryFullTitle;
	private String inventoryFullChat;


	private CooldownMap<Player> inventoryFullCooldown = CooldownMap.create(Cooldown.of(2, TimeUnit.SECONDS));
	private boolean multipliersModule;

	public UltraPrisonAutoSell(UltraPrisonCore UltraPrisonCore) {
		this.core = UltraPrisonCore;
	}

	private void loadMessages() {
		messages = new HashMap<>();
		for (String key : this.getConfig().get().getConfigurationSection("messages").getKeys(false)) {
			messages.put(key.toLowerCase(), Text.colorize(this.getConfig().get().getString("messages." + key)));
		}
	}

	private void loadAutoSellRegions() {
		regionsAutoSell = new HashMap<>();

		ConfigurationSection section = this.getConfig().get().getConfigurationSection("regions");
		if (section == null) {
			return;
		}

		for (String regName : section.getKeys(false)) {

			String worldName = this.getConfig().get().getString("regions." + regName + ".world");

			String permRequired = this.getConfig().get().getString("regions." + regName + ".permission");

			World w = Bukkit.getWorld(worldName);

			if (w == null) {
				this.core.getLogger().warning("There is no such World named " + worldName + "! Perhaps its no loaded yet?");
				continue;
			}

			Optional<IWrappedRegion> optRegion = WorldGuardWrapper.getInstance().getRegion(w, regName);

			if (!optRegion.isPresent()) {
				this.core.getLogger().warning("There is no such WorldGuard region named " + regName + " in world " + worldName);
				continue;
			}

			IWrappedRegion region = optRegion.get();

			if (region == null || w == null) {
				continue;
			}

			Map<CompMaterial, Double> sellPrices = new HashMap<>();

			for (String item : this.getConfig().get().getConfigurationSection("regions." + regName + ".items").getKeys(false)) {
				CompMaterial type = CompMaterial.valueOf(item);
				double sellPrice = this.getConfig().get().getDouble("regions." + regName + ".items." + item);
				sellPrices.put(type, sellPrice);
			}


			regionsAutoSell.put(regName, new SellRegion(region, permRequired, sellPrices));

			this.core.getLogger().info("Loaded auto-sell region named " + regName + " in world " + worldName);

		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {
		this.config.reload();
		this.loadMessages();
		this.broadcastTime = this.getConfig().get().getInt("auto_sell_broadcast.time");
		this.autoSellBroadcastMessage = this.getConfig().get().getStringList("auto_sell_broadcast.message");
		this.enableAutosellAutomatically = this.getConfig().get().getBoolean("enable-autosell-automatically");
		this.autoSmelt = this.getConfig().get().getBoolean("auto-smelt");

		this.inventoryFullNoticiation = this.getConfig().get().getBoolean("inventory_full_notification.enabled");
		this.inventoryFullTitle = this.getConfig().get().getStringList("inventory_full_notification.title");
		this.inventoryFullChat = this.getConfig().get().getString("inventory_full_notification.chat");
		this.multipliersModule = this.core.isModuleEnabled(UltraPrisonMultipliers.MODULE_NAME);
		this.loadAutoSellRegions();
	}

	@Override
	public void enable() {
		this.enabled = true;

		this.config = this.core.getFileManager().getConfig("autosell.yml").copyDefaults(true).save();

		this.enabledAutoSell = new ArrayList<>();
		this.lastEarnings = new HashMap<>();
		this.lastItems = new HashMap<>();

		this.broadcastTime = this.getConfig().get().getInt("auto_sell_broadcast.time");
		this.autoSellBroadcastMessage = this.getConfig().get().getStringList("auto_sell_broadcast.message");
		this.enableAutosellAutomatically = this.getConfig().get().getBoolean("enable-autosell-automatically");
		this.autoSmelt = this.getConfig().get().getBoolean("auto-smelt");

		this.inventoryFullNoticiation = this.getConfig().get().getBoolean("inventory_full_notification.enabled");
		this.inventoryFullTitle = this.getConfig().get().getStringList("inventory_full_notification.title");
		this.inventoryFullChat = this.getConfig().get().getString("inventory_full_notification.chat");
		this.multipliersModule = this.core.isModuleEnabled(UltraPrisonMultipliers.MODULE_NAME);
		this.loadAutoSellRegions();
		this.loadMessages();
		this.registerCommands();
		this.registerListeners();
		this.runBroadcastTask();

		this.api = new UltraPrisonAutoSellAPIImpl(this);
	}

	private void runBroadcastTask() {
		Schedulers.async().runRepeating(() -> {
			Players.all().stream().filter(p -> lastEarnings.containsKey(p.getUniqueId())).forEach(p -> {
				double lastAmount = lastEarnings.getOrDefault(p.getUniqueId(), 0.0);
				if (lastAmount <= 0.0) {
					return;
				}
				long lastItems = this.lastItems.getOrDefault(p.getUniqueId(), 0L);
				for (String s : this.autoSellBroadcastMessage) {
					PlayerUtils.sendMessage(p, Text.colorize(s.replace("%money%", String.format("%,.0f", lastAmount)).replace("%items%", String.format("%,d", lastItems))));
				}
			});
			lastEarnings.clear();
			lastItems.clear();
		}, this.broadcastTime, TimeUnit.SECONDS, this.broadcastTime, TimeUnit.SECONDS);
	}

	private void registerListeners() {
		Events.subscribe(PlayerJoinEvent.class)
				.handler(e -> Schedulers.async().runLater(() -> {
					if (this.enableAutosellAutomatically && e.getPlayer().hasPermission("ultraprison.autosell.toggle") && !enabledAutoSell.contains(e.getPlayer().getUniqueId())) {
						toggleAutoSell(e.getPlayer());
					} else if (enabledAutoSell.contains(e.getPlayer().getUniqueId())) {
						PlayerUtils.sendMessage(e.getPlayer(), getMessage("autosell_enable"));
					}
				}, 20)).bindWith(this.core);
		Events.subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
				.filter(EventFilters.ignoreCancelled())
				.filter(e -> !e.isCancelled() && e.getPlayer().getGameMode() == GameMode.SURVIVAL && e.getPlayer().getItemInHand() != null && this.core.isPickaxeSupported(e.getPlayer().getItemInHand().getType()))
				.handler(e -> {
					int fortuneLevel = core.isModuleEnabled(UltraPrisonEnchants.MODULE_NAME) ? core.getEnchants().getApi().getEnchantLevel(e.getPlayer().getItemInHand(), 3) : e.getPlayer().getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);

					if (!enabledAutoSell.contains(e.getPlayer().getUniqueId())) {

						if (e.getPlayer().getInventory().firstEmpty() != -1) {
							e.getPlayer().getInventory().addItem(new ItemStack(this.autoSmelt ? MaterialUtils.getSmeltedForm(e.getBlock().getType()) : e.getBlock().getType(), 1 + fortuneLevel));
						} else {
							if (this.inventoryFullNoticiation && inventoryFullCooldown.test(e.getPlayer())) {
								if (this.inventoryFullTitle != null && !this.inventoryFullTitle.isEmpty() && !this.inventoryFullTitle.get(0).isEmpty() && !this.inventoryFullTitle.get(1).isEmpty()) {
									e.getPlayer().sendTitle(Text.colorize(this.inventoryFullTitle.get(0)), Text.colorize(this.inventoryFullTitle.get(1)));
								} else if (!this.inventoryFullChat.isEmpty()) {
									PlayerUtils.sendMessage(e.getPlayer(), Text.colorize(this.inventoryFullChat));
								}
							}

						}

						e.getBlock().getDrops().clear();
						e.getBlock().setType(CompMaterial.AIR.getMaterial());
					} else {
						Set<IWrappedRegion> regions = WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation());

						if (regions == null || regions.isEmpty()) {
							return;
						}

						for (IWrappedRegion reg : regions) {
							if (regionsAutoSell.containsKey(reg.getId()) && regionsAutoSell.get(reg.getId()).sellsMaterial(CompMaterial.fromBlock(e.getBlock()))) {

								SellRegion region = regionsAutoSell.get(reg.getId());

								int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;
								double amount = core.isModuleEnabled(UltraPrisonMultipliers.MODULE_NAME) ? core.getMultipliers().getApi().getTotalToDeposit(e.getPlayer(), (regionsAutoSell.get(reg.getId()).getSellPriceFor(CompMaterial.fromBlock(e.getBlock())) + 0.0) * amplifier, MultiplierType.SELL) : (regionsAutoSell.get(reg.getId()).getSellPriceFor(CompMaterial.fromBlock(e.getBlock())) + 0.0) * amplifier;

								UltraPrisonAutoSellEvent event = new UltraPrisonAutoSellEvent(e.getPlayer(), region, e.getBlock(), amount);

								Events.call(event);

								if (event.isCancelled()) {
									return;
								}

								amount = event.getMoneyToDeposit();

								int amountOfItems = 0;

								for (ItemStack item : e.getBlock().getDrops(e.getPlayer().getItemInHand())) {
									amountOfItems += item.getAmount() * amplifier;
								}

								boolean luckyBooster = core.isModuleEnabled(UltraPrisonEnchants.MODULE_NAME) && LuckyBoosterEnchant.hasLuckyBoosterRunning(e.getPlayer());

								core.getEconomy().depositPlayer(e.getPlayer(), luckyBooster ? amount * 2 : amount);
								this.addToCurrentEarnings(e.getPlayer(), luckyBooster ? amount * 2 : amount);

								this.lastItems.put(e.getPlayer().getUniqueId(), this.lastItems.getOrDefault(e.getPlayer().getUniqueId(), 0L) + amountOfItems);

								e.getBlock().getDrops().clear();
								e.getBlock().setType(CompMaterial.AIR.toMaterial());
								break;
							}
						}
					}

				}).bindWith(core);
	}

	@Override
	public void disable() {
		this.enabled = false;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public String[] getTables() {
		return new String[0];
	}

	@Override
	public String[] getCreateTablesSQL(DatabaseType type) {
		return new String[0];
	}

	private void registerCommands() {
		Commands.create()
				.assertPlayer()
				.assertPermission(AUTOSELL_TOGGLE_PERMISSION, getMessage("no_permission_autosell_toggle"))
				.handler(c -> {
					if (c.args().size() == 0) {
						toggleAutoSell(c.sender());
					}
				}).registerAndBind(core, "autosell");
		Commands.create()
				.assertPlayer()
				.assertPermission(ADMIN_PERMISSION)
				.handler(c -> {
					CompMaterial type;
					double price;
					if (c.args().size() == 1) {

						if (c.sender().getItemInHand() == null) {
							PlayerUtils.sendMessage(c.sender(), Text.colorize("&cPlease hold some item!"));
							return;
						}

						price = c.arg(0).parseOrFail(Double.class);
						type = CompMaterial.fromItem(c.sender().getItemInHand());
					} else if (c.args().size() == 2) {
						try {
							type = CompMaterial.fromString(c.rawArg(0));
						} catch (Exception e) {
							PlayerUtils.sendMessage(c.sender(), Text.colorize("&cInvalid material name!"));
							return;
						}
						price = c.arg(1).parseOrFail(Double.class);
					} else {
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&cInvalid usage!"));
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&c/sellprice <material> <price> - Sets the sell price of specified material."));
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&c/sellprice <price> - Sets the sell price of item material you have in your hand."));
						return;
					}

					if (type == null) {
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&cUnable to parse material!"));
						return;
					}

					if (type.name().endsWith("PICKAXE")) {
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&cI think you do not want to let your players sell pickaxes."));
						return;
					}

					if (price <= 0.0) {
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&cSell price needs to be higher than 0!"));
						return;
					}

					IWrappedRegion region = RegionUtils.getFirstRegionAtLocation(c.sender().getLocation());

					if (region == null) {
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&cYou must be standing in a region!"));
						return;
					}

					getConfig().set("regions." + region.getId() + ".world", c.sender().getWorld().getName());
					getConfig().set("regions." + region.getId() + ".items." + type.name(), price);
					getConfig().save();

					SellRegion sellRegion;

					if (regionsAutoSell.containsKey(region.getId())) {
						sellRegion = regionsAutoSell.get(region.getId());
					} else {
						sellRegion = new SellRegion(region, null, new HashMap<>());
					}

					sellRegion.addSellPrice(type, price);
					regionsAutoSell.put(region.getId(), sellRegion);

					PlayerUtils.sendMessage(c.sender(), Text.colorize(String.format("&aSuccessfuly set sell price of &e%s &ato &e$%.2f &ain region &e%s", type.name(), price, region.getId())));

				}).registerAndBind(core, "sellprice");
		Commands.create()
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() == 0 || c.args().size() == 1) {

						IWrappedRegion region;

						if (c.args().size() == 1) {

							String regionName = c.rawArg(0);

							Optional<IWrappedRegion> optRegion = WorldGuardWrapper.getInstance().getRegion(c.sender().getLocation().getWorld(), regionName);

							if (!optRegion.isPresent()) {
								PlayerUtils.sendMessage(c.sender(), getMessage("invalid_region"));
								return;
							}

							region = optRegion.get();
						} else {
							region = RegionUtils.getRegionWithHighestPriority(c.sender().getLocation());
						}

						sellAll(c.sender(), region, true);
					}
				}).registerAndBind(core, "sellall");

	}

	public void sellAll(Player sender, IWrappedRegion region, boolean sendMessage) {

		if (region == null) {
			if (sendMessage) {
				PlayerUtils.sendMessage(sender, this.getMessage("not_in_region"));
			}
			return;
		}

		this.core.debug("User " + sender.getName() + " ran /sellall in region " + region.getId(), this);

		if (regionsAutoSell.containsKey(region.getId())) {

				SellRegion sellRegion = regionsAutoSell.get(region.getId());

				if (sellRegion.getPermissionRequired() != null && !sellRegion.getPermissionRequired().isEmpty() && !sender.hasPermission(sellRegion.getPermissionRequired())) {
					PlayerUtils.sendMessage(sender, getMessage("no_permission_sell").replace("%perm%", sellRegion.getPermissionRequired()));
					return;
				}

				double totalPrice = 0;

				List<ItemStack> toRemove = new ArrayList<>();

			for (CompMaterial m : sellRegion.getSellingMaterials()) {
				for (ItemStack item : Arrays.stream(sender.getInventory().getContents()).filter(i -> i != null && CompMaterial.fromItem(i) == m).collect(Collectors.toList())) {
						totalPrice += item.getAmount() * sellRegion.getSellPriceFor(m);
						toRemove.add(item);
					}
				}

				toRemove.forEach(i -> sender.getInventory().removeItem(i));

				if (this.multipliersModule) {
					totalPrice = (long) core.getMultipliers().getApi().getTotalToDeposit(sender, totalPrice, MultiplierType.SELL);
				}

				UltraPrisonSellAllEvent event = new UltraPrisonSellAllEvent(sender, sellRegion, totalPrice);

				Events.callSync(event);

				if (event.isCancelled()) {
					this.core.debug("UltraPrisonSellAllEvent was cancelled.", this);
					return;
				}

				core.getEconomy().depositPlayer(sender, event.getSellPrice());

				if (event.getSellPrice() > 0.0 && sendMessage) {
					PlayerUtils.sendMessage(sender, getMessage("sell_all_complete").replace("%price%", String.format("%,.0f", event.getSellPrice())));
				}
		}
	}

	private void toggleAutoSell(Player player) {
		if (!enabledAutoSell.contains(player.getUniqueId())) {
			PlayerUtils.sendMessage(player, getMessage("autosell_enable"));
			enabledAutoSell.add(player.getUniqueId());
		} else {
			enabledAutoSell.remove(player.getUniqueId());
			PlayerUtils.sendMessage(player, getMessage("autosell_disable"));
		}
	}

	public String getMessage(String key) {
		return messages.get(key.toLowerCase());
	}

	public double getCurrentEarnings(Player player) {
		return lastEarnings.getOrDefault(player.getUniqueId(), 0.0);
	}

	public double getPriceForBrokenBlock(String regionName, Block block) {
		return regionsAutoSell.containsKey(regionName) ? regionsAutoSell.get(regionName).getSellPriceFor(CompMaterial.fromBlock(block)) : 0.0;
	}

	public boolean hasAutoSellEnabled(Player p) {
		return enabledAutoSell.contains(p.getUniqueId());
	}

	public void addToCurrentEarnings(Player p, double amount) {
		double current = this.lastEarnings.getOrDefault(p.getUniqueId(), 0.0);

		this.lastEarnings.put(p.getUniqueId(), current + amount);
	}

	public Collection<SellRegion> getAutoSellRegions() {
		return this.regionsAutoSell.values();
	}

	public SellRegion getAutoSellRegion(Location location) {
		for (SellRegion region : this.regionsAutoSell.values()) {
			if (region.contains(location)) {
				return region;
			}
		}
		return null;
	}
}
