package me.drawethree.wildprisoncore.autominer.gui;

import me.drawethree.wildprisoncore.autominer.WildPrisonAutoMiner;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class LevelAutoMinerGui extends Gui {

	private static final String TITLE = Text.colorize(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_gui.title"));
	private static final int LINES = WildPrisonAutoMiner.getInstance().getConfig().get().getInt("level_up_gui.lines");

	private static final ItemStack EMPTY_SLOT_ITEM = ItemStackBuilder.
			of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_gui.empty_slots").split(":")[0]))
			.data(Integer.parseInt(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_gui.empty_slots").split(":")[1])).build();

	private static final ItemStack HELP_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_gui.help_item.material")))
			.name(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_gui.help_item.name")).lore(WildPrisonAutoMiner.getInstance().getConfig().get().getStringList("level_up_gui.help_item.lore")).build();

	private static final ItemStack BACK_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_gui.back_item.material")))
			.name(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_gui.back_item.name")).lore(WildPrisonAutoMiner.getInstance().getConfig().get().getStringList("level_up_gui.back_item.lore")).build();

	private static final ItemStack LOCKED_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_gui.locked_item.material")))
			.name(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_gui.locked_item.name")).lore(WildPrisonAutoMiner.getInstance().getConfig().get().getStringList("level_up_gui.locked_item.lore")).build();

	private static final ItemStack LOCKED_NEXT_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_gui.locked_next_item.material")))
			.name(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_gui.locked_next_item.name")).lore(WildPrisonAutoMiner.getInstance().getConfig().get().getStringList("level_up_gui.locked_next_item.lore")).build();

	private static int HELP_ITEM_SLOT = WildPrisonAutoMiner.getInstance().getConfig().get().getInt("level_up_gui.help_item.slot");
	private static int BACK_ITEM_SLOT = WildPrisonAutoMiner.getInstance().getConfig().get().getInt("level_up_gui.back_item.slot");

	public LevelAutoMinerGui(Player player) {
		super(player, LINES, TITLE);
	}

	@Override
	public void redraw() {

		int playerLevel = WildPrisonAutoMiner.getInstance().getPlayerLevel(this.getPlayer());

		if (isFirstDraw()) {

			for (int i = 0; i < this.getHandle().getSize(); i++) {
				this.setItem(i, ItemStackBuilder.of(EMPTY_SLOT_ITEM).buildItem().build());
			}
			this.setItem(HELP_ITEM_SLOT, ItemStackBuilder.of(HELP_ITEM).buildItem().build());
			this.setItem(BACK_ITEM_SLOT, ItemStackBuilder.of(BACK_ITEM).build(() -> {
				this.close();
				new MainAutoMinerGui(this.getPlayer()).open();
			}));
		}

		for (WildPrisonAutoMiner.AutoMinerFuelLevel level : WildPrisonAutoMiner.getInstance().getFuelLevels()) {
			if (playerLevel >= level.getLevel()) {
				this.setItem(level.getGuiItemSlot(), ItemStackBuilder.of(level.getGuiItem()).buildItem().build());
			} else if (level.getLevel() == playerLevel + 1) {
				this.setItem(level.getGuiItemSlot(), ItemStackBuilder.of(this.getNextLevelItem()).build(() -> {
					if (WildPrisonAutoMiner.getInstance().tryBuyNextLevel(this.getPlayer())) {
						this.redraw();
					}
				}));
			} else {
				this.setItem(level.getGuiItemSlot(), ItemStackBuilder.of(LOCKED_ITEM).buildItem().build());
			}
		}
	}

	private ItemStack getNextLevelItem() {
		ItemStack item = LOCKED_NEXT_ITEM.clone();
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		for (int i = 0; i < lore.size(); i++) {
			lore.set(i, lore.get(i).replace("%cost%", String.format("%,d", WildPrisonAutoMiner.getInstance().getNextLevel(this.getPlayer()).getCost())));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
}
