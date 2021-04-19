package me.drawethree.ultraprisoncore.enchants.gui;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.drawethree.ultraprisoncore.utils.SkullUtils;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text.Text;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DisenchantGUI extends Gui {

	private static String GUI_TITLE;
	private static Item EMPTY_SLOT_ITEM;
	private static Item HELP_ITEM;

	private static int HELP_ITEM_SLOT;
	private static int PICKAXE_ITEM_SLOT;
	private static int GUI_LINES;


	static {
		reload();
	}


	@Getter
	@Setter
	private ItemStack pickAxe;

	@Getter
	private int pickaxePlayerInventorySlot;

	public DisenchantGUI(Player player, ItemStack pickAxe, int pickaxePlayerInventorySlot) {
		super(player, GUI_LINES, GUI_TITLE);

		this.pickAxe = pickAxe;
		this.pickaxePlayerInventorySlot = pickaxePlayerInventorySlot;
	}

	@Override
	public void redraw() {
		// perform initial setup.
		if (isFirstDraw()) {
			for (int i = 0; i < this.getHandle().getSize(); i++) {
				this.setItem(i, EMPTY_SLOT_ITEM);
			}
			this.setItem(HELP_ITEM_SLOT, HELP_ITEM);
		}

		for (UltraPrisonEnchantment enchantment : UltraPrisonEnchantment.all()) {
			if (!enchantment.isRefundEnabled() || !enchantment.isEnabled()) {
				continue;
			}
			int level = UltraPrisonEnchants.getInstance().getEnchantsManager().getEnchantLevel(this.pickAxe, enchantment.getId());
			this.setItem(enchantment.refundGuiSlot(), UltraPrisonEnchants.getInstance().getEnchantsManager().getRefundGuiItem(enchantment, this, level));
		}

		this.setItem(PICKAXE_ITEM_SLOT, Item.builder(pickAxe).build());
	}

	public static void reload() {


		GUI_TITLE = Text.colorize(UltraPrisonEnchants.getInstance().getConfig().get().getString("disenchant_menu.title"));
		EMPTY_SLOT_ITEM = ItemStackBuilder.
				of(CompMaterial.fromString(UltraPrisonEnchants.getInstance().getConfig().get().getString("disenchant_menu.empty_slots")).toItem()).buildItem().build();

		String base64 = UltraPrisonEnchants.getInstance().getConfig().get().getString("disenchant_menu.help_item.Base64", null);

		if (base64 != null) {
			HELP_ITEM = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(base64))
					.name(UltraPrisonEnchants.getInstance().getConfig().get().getString("disenchant_menu.help_item.name")).lore(UltraPrisonEnchants.getInstance().getConfig().get().getStringList("disenchant_menu.help_item.lore")).buildItem().build();
		} else {
			HELP_ITEM = ItemStackBuilder.of(CompMaterial.fromString(UltraPrisonEnchants.getInstance().getConfig().get().getString("disenchant_menu.help_item.material")).toMaterial())
					.name(UltraPrisonEnchants.getInstance().getConfig().get().getString("disenchant_menu.help_item.name")).lore(UltraPrisonEnchants.getInstance().getConfig().get().getStringList("disenchant_menu.help_item.lore")).buildItem().build();
		}

		HELP_ITEM_SLOT = UltraPrisonEnchants.getInstance().getConfig().get().getInt("disenchant_menu.help_item.slot");
		PICKAXE_ITEM_SLOT = UltraPrisonEnchants.getInstance().getConfig().get().getInt("disenchant_menu.pickaxe_slot");
		GUI_LINES = UltraPrisonEnchants.getInstance().getConfig().get().getInt("disenchant_menu.lines");
	}
}
