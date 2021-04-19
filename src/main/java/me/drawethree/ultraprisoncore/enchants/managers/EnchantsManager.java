package me.drawethree.ultraprisoncore.enchants.managers;

import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;
import me.drawethree.ultraprisoncore.api.events.player.UltraPrisonPlayerEnchantEvent;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.drawethree.ultraprisoncore.enchants.gui.DisenchantGUI;
import me.drawethree.ultraprisoncore.enchants.gui.EnchantGUI;
import me.drawethree.ultraprisoncore.pickaxelevels.model.PickaxeLevel;
import me.drawethree.ultraprisoncore.utils.SkullUtils;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text.Text;
import me.lucko.helper.time.Time;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnchantsManager {

	public static final String NBT_TAG_INDETIFIER = "ultra-prison-ench-";

	private final UltraPrisonEnchants plugin;
	private List<String> ENCHANT_GUI_ITEM_LORE;
	private List<String> DISENCHANT_GUI_ITEM_LORE;
	private List<String> PICKAXE_LORE;
	@Getter
	private boolean openEnchantMenuOnRightClickBlock;
	@Getter
	private boolean allowEnchantsOutside;

	@Getter
	private double refundPercentage;

	public EnchantsManager(UltraPrisonEnchants plugin) {
		this.plugin = plugin;
		this.reload();
	}


	public HashMap<UltraPrisonEnchantment, Integer> getPlayerEnchants(ItemStack pickAxe) {
		HashMap<UltraPrisonEnchantment, Integer> returnMap = new HashMap<>();
		for (UltraPrisonEnchantment enchantment : UltraPrisonEnchantment.all()) {
			int level = this.getEnchantLevel(pickAxe, enchantment.getId());
			if (level == 0) {
				continue;
			}
			returnMap.put(enchantment, level);
		}
		return returnMap;
	}


	public ItemStack findPickaxe(Player p) {
		for (ItemStack i : p.getInventory()) {
			if (i == null) {
				continue;
			}
			if (i.getType() == CompMaterial.DIAMOND_PICKAXE.toMaterial()) {
				return i;
			}
		}
		return null;
	}

	public void updatePickaxe(ItemStack item) {
		if (item == null || item.getType() != Material.DIAMOND_PICKAXE) {
			return;
		}

		this.applyLoreToPickaxe(item);
	}


	private void applyLoreToPickaxe(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<>();

		for (String s : PICKAXE_LORE) {
			s = s.replace("%Blocks%", String.valueOf(getBlocksBroken(item)));

			if (this.plugin.getCore().isModuleEnabled("Pickaxe Levels")) {
				PickaxeLevel currentLevel = this.plugin.getCore().getPickaxeLevels().getPickaxeLevel(item);
				PickaxeLevel nextLevel = this.plugin.getCore().getPickaxeLevels().getNextPickaxeLevel(currentLevel);
				s = s.replace("%Blocks_Required%", nextLevel == null ? "∞" : String.valueOf(nextLevel.getBlocksRequired()));
				s = s.replace("%PickaxeLevel%", currentLevel == null ? "0" : String.valueOf(currentLevel.getLevel()));
				s = s.replace("%PickaxeProgress%", this.plugin.getCore().getPickaxeLevels().getProgressBar(item));
			}

			Pattern pt = Pattern.compile("%Enchant-\\d+%");
			Matcher matcher = pt.matcher(s);

			if (matcher.find()) {
				int enchId = Integer.parseInt(matcher.group().replaceAll("\\D", ""));
				UltraPrisonEnchantment enchantment = UltraPrisonEnchantment.getEnchantById(enchId);
				if (enchantment != null) {
					int enchLvl = getEnchantLevel(item, enchId);
					if (enchLvl > 0) {
						s = s.replace(matcher.group(), enchantment.getName() + " " + enchLvl);
					} else {
						continue;
					}
				} else {
					continue;
				}
			}
			lore.add(Text.colorize(s));
		}

		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
	}

	public long getBlocksBroken(ItemStack item) {

		if (item == null || item.getType() == Material.AIR) {
			return 0;
		}

		NBTItem nbtItem = new NBTItem(item);

		if (!nbtItem.hasKey("blocks-broken")) {
			return 0;
		}

		return nbtItem.getLong("blocks-broken");
	}

	public synchronized void addBlocksBrokenToItem(Player p, int amount) {

		if (amount == 0) {
			return;
		}

		NBTItem nbtItem = new NBTItem(p.getItemInHand());

		try {
			int amountToConvert = nbtItem.getInteger("blocks-broken");

			if (amountToConvert > 0) {
				amount += amountToConvert;
				nbtItem.removeKey("blocks-broken");
			}
		} catch (Exception e) {
			//Nothing to migrate
		}

		if (!nbtItem.hasKey("blocks-broken")) {
			nbtItem.setLong("blocks-broken", 0L);
		}

		nbtItem.setLong("blocks-broken", nbtItem.getLong("blocks-broken") + amount);

		p.setItemInHand(nbtItem.getItem());
		applyLoreToPickaxe(p.getItemInHand());
	}

	public boolean hasEnchant(Player p, int id) {
		ItemStack item = findPickaxe(p);
		if (item == null) {
			return false;
		}
		return getEnchantLevel(item, id) != 0;
	}

	public synchronized int getEnchantLevel(ItemStack itemStack, int id) {

		if (itemStack == null || itemStack.getType() == Material.AIR) {
			return 0;
		}

		NBTItem nbtItem = new NBTItem(itemStack);

		if (!nbtItem.hasKey(NBT_TAG_INDETIFIER + id)) {
			return 0;
		}
		return nbtItem.getInteger(NBT_TAG_INDETIFIER + id);
	}

	public void handleBlockBreak(BlockBreakEvent e, ItemStack pickAxe) {
		HashMap<UltraPrisonEnchantment, Integer> playerEnchants = this.getPlayerEnchants(pickAxe);
		for (UltraPrisonEnchantment enchantment : playerEnchants.keySet()) {
			enchantment.onBlockBreak(e, playerEnchants.get(enchantment));
		}
	}

	public void handlePickaxeEquip(Player p, ItemStack newItem) {
		HashMap<UltraPrisonEnchantment, Integer> playerEnchants = this.getPlayerEnchants(newItem);
		for (UltraPrisonEnchantment enchantment : playerEnchants.keySet()) {
			enchantment.onEquip(p, newItem, playerEnchants.get(enchantment));
		}
	}

	public void handlePickaxeUnequip(Player p, ItemStack newItem) {
		p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
		HashMap<UltraPrisonEnchantment, Integer> playerEnchants = this.getPlayerEnchants(newItem);
		for (UltraPrisonEnchantment enchantment : playerEnchants.keySet()) {
			enchantment.onUnequip(p, newItem, playerEnchants.get(enchantment));
		}
	}


	public int getInventorySlot(Player player, ItemStack item) {
		for (int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack item1 = player.getInventory().getItem(i);

			if (item1 == null) {
				continue;
			}

			if (item1.equals(item)) {
				return i;
			}
		}
		return -1;
	}

	public ItemStack addEnchant(Player p, ItemStack item, int id, int level) {
		UltraPrisonEnchantment enchantment = UltraPrisonEnchantment.getEnchantById(id);

		if (enchantment == null || item == null) {
			return item;
		}

		NBTItem nbtItem = new NBTItem(item, true);

		if (level > 0) {
			nbtItem.setInteger(EnchantsManager.NBT_TAG_INDETIFIER + enchantment.getId(), level);
		}

		if (!nbtItem.hasKey("pickaxe-id")) {
			nbtItem.setString("pickaxe-id", UUID.randomUUID().toString());
		}

		nbtItem.mergeCustomNBT(item);
		this.applyLoreToPickaxe(item);
		return item;
	}

	public ItemStack addEnchant(ItemStack item, int id, int level) {
		UltraPrisonEnchantment enchantment = UltraPrisonEnchantment.getEnchantById(id);

		if (enchantment == null || item == null) {
			return item;
		}

		NBTItem nbtItem = new NBTItem(item);

		if (level > 0) {
			nbtItem.setInteger(EnchantsManager.NBT_TAG_INDETIFIER + enchantment.getId(), level);
		}

		if (!nbtItem.hasKey("pickaxe-id")) {
			nbtItem.setString("pickaxe-id", UUID.randomUUID().toString());
		}

		item = nbtItem.getItem();
		this.applyLoreToPickaxe(item);
		return item;
	}

	public void addEnchant(Player p, ItemStack item, UltraPrisonEnchantment enchantment, int level) {
		this.addEnchant(p, item, enchantment.getId(), level);
	}

	public boolean removeEnchant(Player p, int id) {
		UltraPrisonEnchantment enchantment = UltraPrisonEnchantment.getEnchantById(id);
		if (enchantment == null || p.getItemInHand() == null) {
			return false;
		}

		ItemStack item = p.getItemInHand();

		NBTItem nbtItem = new NBTItem(item);


		nbtItem.removeKey(NBT_TAG_INDETIFIER + id);

		p.setItemInHand(nbtItem.getItem());
		applyLoreToPickaxe(p.getItemInHand());
		return true;
	}

	public ItemStack removeEnchant(ItemStack item, Player p, int id, int level) {
		UltraPrisonEnchantment enchantment = UltraPrisonEnchantment.getEnchantById(id);

		if (enchantment == null || item == null || level == 0) {
			return item;
		}

		NBTItem nbtItem = new NBTItem(item);

		nbtItem.setInteger(NBT_TAG_INDETIFIER + id, level - 1);

		nbtItem.mergeCustomNBT(item);
		this.applyLoreToPickaxe(item);
		return item;
	}

	public boolean buyEnchnant(UltraPrisonEnchantment enchantment, EnchantGUI gui, int currentLevel, int addition) {

		if (currentLevel >= enchantment.getMaxLevel()) {
			gui.getPlayer().sendMessage(plugin.getMessage("enchant_max_level"));
			return false;
		}

		if (currentLevel + addition > enchantment.getMaxLevel()) {
			gui.getPlayer().sendMessage(plugin.getMessage("enchant_max_level_exceed"));
			return false;
		}

		long totalCost = 0;

		long startTime = Time.nowMillis();

		for (int j = 0; j < addition; j++) {
			totalCost += enchantment.getCostOfLevel(currentLevel + j + 1);
		}

		this.plugin.getCore().debug(String.format("Enchants | Calculation of levels %,d - %,d of %s enchant took %dms", currentLevel + 1, currentLevel + addition + 1, enchantment.getRawName(), Time.nowMillis() - startTime));

		if (!plugin.getCore().getTokens().getApi().hasEnough(gui.getPlayer(), totalCost)) {
			gui.getPlayer().sendMessage(plugin.getMessage("not_enough_tokens"));
			return false;
		}

		UltraPrisonPlayerEnchantEvent event = new UltraPrisonPlayerEnchantEvent(gui.getPlayer(), totalCost, currentLevel + addition);

		Events.call(event);

		if (event.isCancelled()) {
			return false;
		}

		plugin.getCore().getTokens().getApi().removeTokens(gui.getPlayer(), totalCost);

		this.addEnchant(gui.getPlayer(), gui.getPickAxe(), enchantment.getId(), currentLevel + addition);
		gui.getPlayer().getInventory().setItem(gui.getPickaxePlayerInventorySlot(), gui.getPickAxe());

		if (addition == 1) {
			gui.getPlayer().sendMessage(plugin.getMessage("enchant_bought").replace("%tokens%", String.valueOf(totalCost)));
		} else {
			gui.getPlayer().sendMessage(plugin.getMessage("enchant_bought_multiple")
					.replace("%amount%", String.valueOf(addition))
					.replace("%enchant%", enchantment.getName())
					.replace("%tokens%", String.format("%,d", totalCost)));
		}
		return true;
	}

	public boolean disenchant(UltraPrisonEnchantment enchantment, DisenchantGUI gui, int currentLevel,
							  int substraction) {


		if (currentLevel <= 0) {
			gui.getPlayer().sendMessage(plugin.getMessage("enchant_no_level"));
			return false;
		}

		long totalRefunded = 0;

		for (int i = 0; i < substraction; i++, currentLevel--) {

			if (currentLevel <= 0) {
				break;
			}

			long cost = enchantment.getCostOfLevel(currentLevel);

			plugin.getCore().getTokens().getApi().addTokens(gui.getPlayer(), cost);

			this.removeEnchant(gui.getPickAxe(), gui.getPlayer(), enchantment.getId(), currentLevel);
			gui.getPlayer().getInventory().setItem(gui.getPickaxePlayerInventorySlot(), gui.getPickAxe());

			totalRefunded += (cost * (this.refundPercentage / 100.0));
		}

		gui.getPlayer().sendMessage(plugin.getMessage("enchant_refunded").replace("%amount%", String.valueOf(substraction)).replace("%enchant%", enchantment.getName()));
		gui.getPlayer().sendMessage(plugin.getMessage("enchant_tokens_back").replace("%tokens%", String.valueOf(totalRefunded)));
		return true;
	}

	public Item getRefundGuiItem(UltraPrisonEnchantment enchantment, DisenchantGUI gui, int level) {
		Material m = enchantment.isRefundEnabled() ? enchantment.getMaterial() : CompMaterial.BARRIER.toMaterial();
		ItemStackBuilder builder = ItemStackBuilder.of(m);

		if (enchantment.getBase64() != null && !enchantment.getBase64().isEmpty()) {
			builder = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(enchantment.getBase64()));
		}

		builder.name(enchantment.isRefundEnabled() ? enchantment.getName() : this.plugin.getMessage("enchant_cant_disenchant"));
		builder.lore(enchantment.isRefundEnabled() ? translateLore(enchantment, DISENCHANT_GUI_ITEM_LORE, level) : new ArrayList<>());

		return enchantment.isRefundEnabled() ? builder.buildItem().bind(handler -> {
			if (handler.getClick() == ClickType.MIDDLE || handler.getClick() == ClickType.SHIFT_RIGHT) {
				this.disenchant(enchantment, gui, level, 100);
				gui.redraw();
			} else if (handler.getClick() == ClickType.LEFT) {
				this.disenchant(enchantment, gui, level, 1);
				gui.redraw();
			} else if (handler.getClick() == ClickType.RIGHT) {
				this.disenchant(enchantment, gui, level, 10);
				gui.redraw();
			}
		}, ClickType.MIDDLE, ClickType.SHIFT_RIGHT, ClickType.LEFT, ClickType.RIGHT).build() : builder.buildConsumer(handler -> handler.getWhoClicked().sendMessage(this.plugin.getMessage("enchant_cant_disenchant")));
	}

	public Item getGuiItem(UltraPrisonEnchantment enchantment, EnchantGUI gui, int currentLevel) {

		ItemStackBuilder builder = ItemStackBuilder.of(enchantment.getMaterial());

		if (enchantment.getBase64() != null && !enchantment.getBase64().isEmpty()) {
			builder = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(enchantment.getBase64()));
		}

		builder.name(enchantment.getName());
		builder.lore(translateLore(enchantment, ENCHANT_GUI_ITEM_LORE, currentLevel));

		return builder.buildItem().bind(handler -> {
			if (!enchantment.canBeBought(gui.getPickAxe())) {
				return;
			}
			if (handler.getClick() == ClickType.MIDDLE || handler.getClick() == ClickType.SHIFT_RIGHT) {
				this.buyEnchnant(enchantment, gui, currentLevel, 100);
				gui.redraw();
			} else if (handler.getClick() == ClickType.LEFT) {
				this.buyEnchnant(enchantment, gui, currentLevel, 1);
				gui.redraw();
			} else if (handler.getClick() == ClickType.RIGHT) {
				this.buyEnchnant(enchantment, gui, currentLevel, 10);
				gui.redraw();
			}
		}, ClickType.MIDDLE, ClickType.SHIFT_RIGHT, ClickType.RIGHT, ClickType.LEFT).build();
	}

	private List<String> translateLore(UltraPrisonEnchantment enchantment, List<String> guiItemLore,
									   int currentLevel) {
		List<String> newList = new ArrayList<>();
		for (String s : guiItemLore) {
			if (s.contains("%description%")) {
				newList.addAll(enchantment.getDescription());
				continue;
			}
			newList.add(s
					.replace("%refund%", String.format("%,d", this.getRefundForLevel(enchantment, currentLevel)))
					.replace("%cost%", String.format("%,d", enchantment.getCost() + (enchantment.getIncreaseCost() * currentLevel)))
					.replace("%max_level%", enchantment.getMaxLevel() == Integer.MAX_VALUE ? "Unlimited" : String.format("%,d", enchantment.getMaxLevel()))
					.replace("%current_level%", String.format("%,d", currentLevel))
					.replace("%pickaxe_level%", String.format("%,d", enchantment.getRequiredPickaxeLevel())));
		}
		return newList;
	}

	private long getRefundForLevel(UltraPrisonEnchantment enchantment, int currentLevel) {
		return (long) ((enchantment.getCost() + (enchantment.getIncreaseCost() * (currentLevel - 1))) * (this.refundPercentage / 100.0));
	}

	public long getPickaxeValue(ItemStack pickAxe) {
		long sum = 0;
		HashMap<UltraPrisonEnchantment, Integer> playerEnchants = this.getPlayerEnchants(pickAxe);
		for (UltraPrisonEnchantment enchantment : playerEnchants.keySet()) {
			for (int i = 1; i <= playerEnchants.get(enchantment); i++) {
				sum += enchantment.getCostOfLevel(i);
			}
		}
		return sum;
	}

	public void reload() {
		this.ENCHANT_GUI_ITEM_LORE = plugin.getConfig().get().getStringList("enchant_menu.item.lore");
		this.DISENCHANT_GUI_ITEM_LORE = plugin.getConfig().get().getStringList("disenchant_menu.item.lore");
		this.PICKAXE_LORE = plugin.getConfig().get().getStringList("Pickaxe.lore");
		this.openEnchantMenuOnRightClickBlock = plugin.getConfig().get().getBoolean("open-menu-on-right-click-block");
		this.allowEnchantsOutside = plugin.getConfig().get().getBoolean("allow-enchants-outside-mine-regions");
		this.refundPercentage = plugin.getConfig().get().getDouble("refund-percentage");
	}

	// /givepickaxe <player> <enchant:18=1;...>
	public void givePickaxe(Player target, String input, CommandSender sender) {
		ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE, 1);

		String[] split = input.split(",");

		for (String s : split) {
			String[] enchantData = s.split("=");

			try {
				UltraPrisonEnchantment enchantment = UltraPrisonEnchantment.getEnchantByName(enchantData[0]);
				if (enchantment == null) {
					enchantment = UltraPrisonEnchantment.getEnchantById(Integer.parseInt(enchantData[0]));
				}

				if (enchantment == null) {
					continue;
				}

				int enchantLevel = Integer.parseInt(enchantData[1]);
				pickaxe = this.addEnchant(pickaxe, enchantment.getId(), enchantLevel);
			} catch (Exception e) {
				continue;
			}
		}

		this.applyLoreToPickaxe(pickaxe);

		if (target == null && sender instanceof Player) {
			target = (Player) sender;
		}

		if (target != null) {
			if (target.getInventory().firstEmpty() == -1) {
				sender.sendMessage(this.plugin.getMessage("pickaxe_inventory_full").replace("%player%", target.getName()));
				return;
			}

			target.getInventory().addItem(pickaxe);
			sender.sendMessage(this.plugin.getMessage("pickaxe_given").replace("%player%", target.getName()));
			target.sendMessage(this.plugin.getMessage("pickaxe_received").replace("%sender%", sender.getName()));
		}
	}

	private ItemStack addUniqueTagToPickaxe(ItemStack pickaxe) {

		NBTItem nbtItem = new NBTItem(pickaxe);


		if (!nbtItem.hasKey("pickaxe-id")) {
			nbtItem.setString("pickaxe-id", UUID.randomUUID().toString());
		}

		pickaxe = nbtItem.getItem();
		return pickaxe;
	}

	public boolean hasEnchants(ItemStack item) {
		return !this.getPlayerEnchants(item).isEmpty();
	}
}
