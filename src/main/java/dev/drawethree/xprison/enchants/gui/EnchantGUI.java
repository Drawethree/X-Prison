package dev.drawethree.xprison.enchants.gui;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.enchants.utils.GuiUtils;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import dev.drawethree.xprison.utils.misc.SkullUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Getter;
import lombok.Setter;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public final class EnchantGUI extends Gui {

	private static List<String> GUI_ITEM_LORE;
	private static String GUI_TITLE;
	private static Item EMPTY_SLOT_ITEM;
	private static int PICKAXE_ITEM_SLOT;
	private static int HELP_ITEM_SLOT;
	private static int DISENCHANT_ITEM_SLOT;
	private static int GUI_LINES;
	private static Item HELP_ITEM;
	private static ItemStack DISENCHANT_ITEM;
	private static boolean PICKAXE_ITEM_ENABLED;
	private static boolean HELP_ITEM_ENABLED;
	private static boolean DISENCHANT_ITEM_ENABLED;

	@Getter
	@Setter
	private ItemStack pickAxe;

	@Getter
	private final int pickaxePlayerInventorySlot;

	private final XPrisonEnchants plugin;

	public EnchantGUI(XPrisonEnchants plugin, Player player, ItemStack pickAxe, int pickaxePlayerInventorySlot) {
		super(player, GUI_LINES, GUI_TITLE);
		this.plugin = plugin;
		this.pickAxe = pickAxe;
		this.pickaxePlayerInventorySlot = pickaxePlayerInventorySlot;

		Events.subscribe(InventoryCloseEvent.class, EventPriority.LOWEST)
				.filter(e -> e.getInventory().equals(this.getHandle()))
				.handler(e -> {
					XPrison.getInstance().getEnchants().getEnchantsManager().handlePickaxeUnequip(this.getPlayer(), this.pickAxe);
					XPrison.getInstance().getEnchants().getEnchantsManager().handlePickaxeEquip(this.getPlayer(), this.pickAxe);
				}).bindWith(this);

		// Checking for duping
		Schedulers.sync().runLater(() -> {
			if (!pickAxe.equals(this.getPlayer().getInventory().getItem(this.pickaxePlayerInventorySlot))) {
				this.close();
			}
		},10);
	}

	@Override
	public void redraw() {

		// perform initial setup.
		if (isFirstDraw()) {
			for (int i = 0; i < this.getHandle().getSize(); i++) {
				this.setItem(i, EMPTY_SLOT_ITEM);
			}
		}

		if (HELP_ITEM_ENABLED) {
			this.setItem(HELP_ITEM_SLOT, HELP_ITEM);
		}

		if (DISENCHANT_ITEM_ENABLED) {
			this.setItem(DISENCHANT_ITEM_SLOT, ItemStackBuilder.of(DISENCHANT_ITEM).build(() -> {
				this.close();
				new DisenchantGUI(this.plugin, this.getPlayer(), this.pickAxe, this.pickaxePlayerInventorySlot).open();
			}));
		}

		if (PICKAXE_ITEM_ENABLED) {
			this.setItem(PICKAXE_ITEM_SLOT, Item.builder(this.pickAxe).build());
		}

		Collection<XPrisonEnchantment> allEnchants = this.plugin.getEnchantsRepository().getAll();
		for (XPrisonEnchantment enchantment : allEnchants) {
			if (!enchantment.isEnabled()) {
				continue;
			}
			int level = XPrisonEnchants.getInstance().getEnchantsManager().getEnchantLevel(this.pickAxe, enchantment);
			this.setItem(enchantment.getGuiSlot(), getGuiItem(enchantment, this, level));
		}
	}

	private Item getGuiItem(XPrisonEnchantment enchantment, EnchantGUI gui, int currentLevel) {

		ItemStackBuilder builder = ItemStackBuilder.of(enchantment.getMaterial());

		if (enchantment.getBase64() != null && !enchantment.getBase64().isEmpty()) {
			builder = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(enchantment.getBase64()));
		}

		builder.name(enchantment.getGuiName());
		builder.lore(GuiUtils.translateGuiLore(enchantment, GUI_ITEM_LORE, currentLevel));

		return builder.buildItem().bind(handler -> {
			if (!enchantment.canBeBought(gui.getPickAxe())) {
				PlayerUtils.sendMessage(this.getPlayer(), this.plugin.getEnchantsConfig().getMessage("pickaxe_level_required").replace("%pickaxe_level%", String.format("%,d", enchantment.getRequiredPickaxeLevel())));
				return;
			}
			if (handler.getClick() == ClickType.MIDDLE || handler.getClick() == ClickType.SHIFT_RIGHT) {
				this.plugin.getEnchantsManager().buyEnchnant(enchantment, gui, currentLevel, 100);
				gui.redraw();
			} else if (handler.getClick() == ClickType.LEFT) {
				this.plugin.getEnchantsManager().buyEnchnant(enchantment, gui, currentLevel, 1);
				gui.redraw();
			} else if (handler.getClick() == ClickType.RIGHT) {
				this.plugin.getEnchantsManager().buyEnchnant(enchantment, gui, currentLevel, 10);
				gui.redraw();
			} else if (handler.getClick() == ClickType.DROP) {
				this.plugin.getEnchantsManager().buyMaxEnchant(enchantment, gui, currentLevel);
			}
		}, ClickType.MIDDLE, ClickType.SHIFT_RIGHT, ClickType.RIGHT, ClickType.LEFT, ClickType.DROP).build();
	}

	public static void init() {

		GUI_ITEM_LORE = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getStringList("enchant_menu.item.lore");
		GUI_TITLE = TextUtils.applyColor(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("enchant_menu.title"));
		EMPTY_SLOT_ITEM = ItemStackBuilder.
				of(CompMaterial.fromString(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("enchant_menu.empty_slots")).toItem()).buildItem().build();
		GUI_LINES = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getInt("enchant_menu.lines");

		HELP_ITEM_ENABLED = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getBoolean("enchant_menu.help_item.enabled", true);
		PICKAXE_ITEM_ENABLED = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getBoolean("enchant_menu.pickaxe_enabled", true);
		DISENCHANT_ITEM_ENABLED = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getBoolean("enchant_menu.disenchant_item.enabled", true);

		if (DISENCHANT_ITEM_ENABLED) {
			String base64 = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("enchant_menu.disenchant_item.Base64", null);

			if (base64 != null) {
				DISENCHANT_ITEM = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(base64))
						.name(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("enchant_menu.disenchant_item.name")).lore(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getStringList("enchant_menu.disenchant_item.lore")).build();
			} else {
				DISENCHANT_ITEM = ItemStackBuilder.of(CompMaterial.fromString(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("enchant_menu.disenchant_item.material")).toMaterial())
						.name(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("enchant_menu.disenchant_item.name")).lore(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getStringList("enchant_menu.disenchant_item.lore")).build();
			}
			DISENCHANT_ITEM_SLOT = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getInt("enchant_menu.disenchant_item.slot");

		}

		if (HELP_ITEM_ENABLED) {
			String base64 = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("enchant_menu.help_item.Base64", null);

			if (base64 != null) {
				HELP_ITEM = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(base64))
						.name(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("enchant_menu.help_item.name")).lore(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getStringList("enchant_menu.help_item.lore")).buildItem().build();
			} else {
				HELP_ITEM = ItemStackBuilder.of(CompMaterial.fromString(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("enchant_menu.help_item.material")).toMaterial())
						.name(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("enchant_menu.help_item.name")).lore(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getStringList("enchant_menu.help_item.lore")).buildItem().build();
			}

			HELP_ITEM_SLOT = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getInt("enchant_menu.help_item.slot");

		}

		if (PICKAXE_ITEM_ENABLED) {
			PICKAXE_ITEM_SLOT = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getInt("enchant_menu.pickaxe_slot");
		}
	}
}
