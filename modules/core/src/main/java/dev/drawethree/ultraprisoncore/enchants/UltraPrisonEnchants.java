package dev.drawethree.ultraprisoncore.enchants;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import dev.drawethree.ultraprisoncore.config.FileManager;
import dev.drawethree.ultraprisoncore.database.DatabaseType;
import dev.drawethree.ultraprisoncore.enchants.api.UltraPrisonEnchantsAPI;
import dev.drawethree.ultraprisoncore.enchants.api.UltraPrisonEnchantsAPIImpl;
import dev.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.enchants.enchants.implementations.LuckyBoosterEnchant;
import dev.drawethree.ultraprisoncore.enchants.gui.DisenchantGUI;
import dev.drawethree.ultraprisoncore.enchants.gui.EnchantGUI;
import dev.drawethree.ultraprisoncore.enchants.managers.EnchantsManager;
import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import dev.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import dev.drawethree.ultraprisoncore.utils.compat.MinecraftVersion;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import dev.drawethree.ultraprisoncore.utils.text.TextUtils;
import lombok.Getter;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.utils.Players;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class UltraPrisonEnchants implements UltraPrisonModule {


	public static final String MODULE_NAME = "Enchants";
	@Getter
	private static UltraPrisonEnchants instance;

	@Getter
	private UltraPrisonEnchantsAPI api;

	@Getter
	private EnchantsManager enchantsManager;

	@Getter
	private FileManager.Config config;

	@Getter
	private final UltraPrisonCore core;

	private HashMap<String, String> messages;
	private final List<UUID> disabledLayer = new ArrayList<>();
	private final List<UUID> disabledExplosive = new ArrayList<>();
	private final Map<UUID, List<ItemStack>> respawnItemsMap = new HashMap<>();
	private final CooldownMap<Player> valueCooldown = CooldownMap.create(Cooldown.of(30, TimeUnit.SECONDS));
	private boolean enabled;

	public UltraPrisonEnchants(UltraPrisonCore UltraPrisonCore) {
		instance = this;
		this.core = UltraPrisonCore;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {

		this.config.reload();

		this.loadMessages();

		this.enchantsManager.reload();

		EnchantGUI.reload();
		DisenchantGUI.reload();

		UltraPrisonEnchantment.reloadAll();
	}

	private void loadMessages() {
		messages = new HashMap<>();
		for (String key : getConfig().get().getConfigurationSection("messages").getKeys(false)) {
			messages.put(key, TextUtils.applyColor(getConfig().get().getString("messages." + key)));
		}
	}

	@Override
	public void enable() {
		this.enabled = true;
		this.config = this.core.getFileManager().getConfig("enchants.yml").copyDefaults(true).save();
		this.enchantsManager = new EnchantsManager(this);
		this.api = new UltraPrisonEnchantsAPIImpl(enchantsManager);
		this.loadMessages();
		this.registerCommands();
		this.registerEvents();

		UltraPrisonEnchantment.loadDefaultEnchantments();
	}

	@Override
	public void disable() {
		this.enabled = false;

		for (Player p : Players.all()) {
			p.closeInventory();
		}

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

	@Override
	public boolean isHistoryEnabled() {
		return false;
	}

	private void registerCommands() {


		Commands.create()
				.assertOp()
				.handler(c -> {

					if (c.args().size() == 0) {
						PlayerUtils.sendMessage(c.sender(), "&c/givepickaxe <player> <[enchant1]=[level1],[enchant2]=[level2],...[enchantX]=[levelX]> <pickaxe_name>");
						return;
					}

					String input = null, name = null;
					Player target = null;

					if (c.args().size() == 1) {
						input = c.rawArg(0);
					} else if (c.args().size() == 2) {
						target = c.arg(0).parseOrFail(Player.class);
						input = c.rawArg(1);
					} else if (c.args().size() >= 3) {
						target = c.arg(0).parseOrFail(Player.class);
						input = c.rawArg(1);
						name = StringUtils.join(c.args().subList(2, c.args().size()), " ");
					}

					this.enchantsManager.givePickaxe(target, input, name, c.sender());
				}).registerAndBind(core, "givepickaxe");

		Commands.create()
				.assertOp()
				.handler(c -> {

					if (c.args().size() == 0) {
						PlayerUtils.sendMessage(c.sender(), "&c/givefirstjoinpickaxe <player>");
						return;
					}

					Player target = c.arg(0).parseOrFail(Player.class);

					target.getInventory().addItem(this.enchantsManager.createFirstJoinPickaxe(target));
					PlayerUtils.sendMessage(c.sender(), "&aYou have given first join pickaxe to &e" + target.getName());
				}).registerAndBind(core, "givefirstjoinpickaxe");

		Commands.create()
				.assertPlayer()
				.handler(c -> {
					if (LuckyBoosterEnchant.hasLuckyBoosterRunning(c.sender())) {
						PlayerUtils.sendMessage(c.sender(), getMessage("lucky_mode_timeleft").replace("%timeleft%", LuckyBoosterEnchant.getTimeLeft(c.sender())));
					} else {
						PlayerUtils.sendMessage(c.sender(), getMessage("lucky_mode_disabled"));
					}
				}).registerAndBind(core, "luckybooster");

		Commands.create()
				.assertPlayer()
				.handler(c -> {
					ItemStack pickAxe = c.sender().getItemInHand();

					if (pickAxe == null || !this.getCore().isPickaxeSupported(pickAxe.getType())) {
						PlayerUtils.sendMessage(c.sender(), getMessage("no_pickaxe_found"));
						return;
					}

					int pickaxeSlot = this.enchantsManager.getInventorySlot(c.sender(), pickAxe);
					this.core.debug("Pickaxe slot is: " + pickaxeSlot, this);
					new DisenchantGUI(c.sender(), pickAxe, pickaxeSlot).open();
				}).registerAndBind(core, "disenchant", "dise", "de", "disenchantmenu", "dismenu");

		Commands.create()
				.assertPlayer()
				.handler(c -> {
					ItemStack pickAxe = c.sender().getItemInHand();

					if (pickAxe == null || !this.getCore().isPickaxeSupported(pickAxe.getType())) {
						PlayerUtils.sendMessage(c.sender(), getMessage("no_pickaxe_found"));
						return;
					}

					int pickaxeSlot = this.enchantsManager.getInventorySlot(c.sender(), pickAxe);
					this.core.debug("Pickaxe slot is: " + pickaxeSlot, this);
					new EnchantGUI(c.sender(), pickAxe, pickaxeSlot).open();
				}).registerAndBind(core, "enchantmenu", "enchmenu");

		Commands.create()
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() == 0) {
						toggleExplosive(c.sender());
					}
				}).registerAndBind(core, "explosive");
		Commands.create()
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() == 0) {
						toggleLayer(c.sender());
					}
				}).registerAndBind(core, "layer");
		Commands.create()
				.assertPlayer()
				.assertPermission("ultraprison.value", this.getMessage("value_no_permission"))
				.handler(c -> {

					if (!c.sender().isOp()) {
						if (!valueCooldown.test(c.sender())) {
							PlayerUtils.sendMessage(c.sender(), this.getMessage("value_cooldown").replace("%time%", String.valueOf(valueCooldown.remainingTime(c.sender(), TimeUnit.SECONDS))));
							return;
						}
					}

					ItemStack pickAxe = c.sender().getItemInHand();

					if (pickAxe == null || !this.getCore().isPickaxeSupported(pickAxe.getType())) {
						PlayerUtils.sendMessage(c.sender(), getMessage("value_no_pickaxe"));
						return;
					}

					PlayerUtils.sendMessage(c.sender(), this.getMessage("value_value").replace("%player%", c.sender().getName()).replace("%tokens%", String.format("%,d", this.enchantsManager.getPickaxeValue(pickAxe))));
				}).registerAndBind(core, "value");
	}

	private void toggleLayer(Player sender) {
		if (disabledLayer.contains(sender.getUniqueId())) {
			PlayerUtils.sendMessage(sender, getMessage("layer_enabled"));
			disabledLayer.remove(sender.getUniqueId());
		} else {
			PlayerUtils.sendMessage(sender, getMessage("layer_disabled"));
			disabledLayer.add(sender.getUniqueId());
		}
	}

	private void toggleExplosive(Player sender) {
		if (disabledExplosive.contains(sender.getUniqueId())) {
			PlayerUtils.sendMessage(sender, getMessage("explosive_enabled"));
			disabledExplosive.remove(sender.getUniqueId());
		} else {
			PlayerUtils.sendMessage(sender, getMessage("explosive_disabled"));
			disabledExplosive.add(sender.getUniqueId());
		}
	}

	private void registerEvents() {
		Events.subscribe(PlayerDeathEvent.class, EventPriority.LOWEST)
				.handler(e-> {

					if (!this.enchantsManager.isKeepPickaxesOnDeath()) {
						return;
					}

					List<ItemStack> pickaxes = e.getDrops().stream().filter(itemStack -> this.getCore().isPickaxeSupported(itemStack) && this.enchantsManager.hasEnchants(itemStack)).collect(Collectors.toList());
					e.getDrops().removeAll(pickaxes);

					this.respawnItemsMap.put(e.getEntity().getUniqueId(),pickaxes);

					if (pickaxes.size() > 0 ) {
						this.core.debug("Removed " + e.getEntity().getName() + "'s pickaxes from drops (" + pickaxes.size() + "). Will be given back on respawn.", this);
					} else {
						this.core.debug("No Pickaxes found for player " + e.getEntity().getName() + " (PlayerDeathEvent)", this);
					}
				}).bindWith(core);
		Events.subscribe(PlayerRespawnEvent.class, EventPriority.LOWEST)
				.handler(e -> {
					if (this.respawnItemsMap.containsKey(e.getPlayer().getUniqueId())) {
						this.respawnItemsMap.remove(e.getPlayer().getUniqueId()).forEach(itemStack -> {
							e.getPlayer().getInventory().addItem(itemStack);
						});
					}
				}).bindWith(core);
		Events.subscribe(PlayerInteractEvent.class)
				.filter(e -> e.getItem() != null && this.getCore().isPickaxeSupported(e.getItem().getType()))
				.filter(e -> (e.getAction() == Action.RIGHT_CLICK_AIR || (e.getAction() == Action.RIGHT_CLICK_BLOCK && this.enchantsManager.isOpenEnchantMenuOnRightClickBlock())))
				.handler(e -> {
					e.setCancelled(true);
					ItemStack pickAxe = e.getItem();
					int pickaxeSlot = this.enchantsManager.getInventorySlot(e.getPlayer(), pickAxe);
					this.core.debug("Pickaxe slot is: " + pickaxeSlot, this);
					new EnchantGUI(e.getPlayer(), pickAxe, pickaxeSlot).open();
				}).bindWith(core);
		Events.subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
				.filter(EventFilters.ignoreCancelled())
				.filter(e -> !e.isCancelled() && e.getPlayer().getItemInHand() != null && this.getCore().isPickaxeSupported(e.getPlayer().getItemInHand().getType()))
				.handler(e -> {
					boolean inMineRegion = WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation()).stream().anyMatch(region -> region.getId().toLowerCase().startsWith("mine"));
					enchantsManager.handleBlockBreak(e, e.getPlayer().getItemInHand(), inMineRegion);
				}).bindWith(core);
		Events.subscribe(BlockBreakEvent.class, EventPriority.LOWEST)
				.filter(e -> !e.isCancelled() && e.getPlayer().getItemInHand() != null && this.getCore().isPickaxeSupported(e.getPlayer().getItemInHand().getType()))
				.filter(e -> WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation()).stream().noneMatch(region -> region.getId().toLowerCase().startsWith("mine")))
				.filter(e -> this.enchantsManager.hasEnchants(e.getPlayer().getItemInHand()))
				.handler(e -> e.setCancelled(true)).bindWith(core);
		// Switching pickaxes
		Events.subscribe(PlayerItemHeldEvent.class, EventPriority.HIGHEST)
				.handler(e -> {

					ItemStack newItem = e.getPlayer().getInventory().getItem(e.getNewSlot());
					ItemStack previousItem = e.getPlayer().getInventory().getItem(e.getPreviousSlot());

					if (previousItem != null && this.getCore().isPickaxeSupported(previousItem.getType())) {
						this.enchantsManager.handlePickaxeUnequip(e.getPlayer(), previousItem);
					}

					if (newItem != null && this.getCore().isPickaxeSupported(newItem.getType())) {
						this.enchantsManager.handlePickaxeEquip(e.getPlayer(), newItem);
					}

				}).bindWith(core);
		// Dropping pickaxe
		Events.subscribe(PlayerDropItemEvent.class, EventPriority.HIGHEST)
				.handler(e -> {
					if (this.getCore().isPickaxeSupported(e.getItemDrop().getItemStack())) {
						this.enchantsManager.handlePickaxeUnequip(e.getPlayer(), e.getItemDrop().getItemStack());
					}
				}).bindWith(core);
		//First join pickaxe
		Events.subscribe(PlayerJoinEvent.class)
				.filter(e -> !e.getPlayer().hasPlayedBefore() && this.enchantsManager.isFirstJoinPickaxeEnabled())
				.handler(e -> {
					ItemStack firstJoinPickaxe = this.enchantsManager.createFirstJoinPickaxe(e.getPlayer());
					e.getPlayer().getInventory().addItem(firstJoinPickaxe);
				}).bindWith(core);
		//Grindstone disenchanting - disable
		if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_14)) {
			Events.subscribe(InventoryClickEvent.class)
					.filter(e -> e.getInventory() instanceof GrindstoneInventory)
					.handler(e -> {
						ItemStack item1 = e.getInventory().getItem(0);
						ItemStack item2 = e.getInventory().getItem(1);
						if (e.getSlot() == 2 && (this.enchantsManager.isEnchanted(item1) || this.enchantsManager.isEnchanted(item2))) {
							e.setCancelled(true);
						}
					}).bindWith(core);
		}
	}

	public String getMessage(String key) {
		return messages.getOrDefault(key.toLowerCase(), TextUtils.applyColor("&cMessage " + key + " not found."));
	}

	public boolean hasLayerDisabled(Player p) {
		return disabledLayer.contains(p.getUniqueId());
	}

	public boolean hasExplosiveDisabled(Player p) {
		return disabledExplosive.contains(p.getUniqueId());
	}

	public boolean isAutoSellModule() {
		return this.core.isModuleEnabled(UltraPrisonAutoSell.MODULE_NAME);
	}

	public boolean isMultipliersModule() {
		return this.core.isModuleEnabled(UltraPrisonMultipliers.MODULE_NAME);
	}

	public boolean isMinesModule() {
		return this.core.isModuleEnabled(UltraPrisonMines.MODULE_NAME);
	}
}
