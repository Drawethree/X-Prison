package me.drawethree.wildprisoncore.autominer.gui;

import me.drawethree.wildprisoncore.autominer.WildPrisonAutoMiner;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LevelMiscAutoMinerGui extends Gui {

	private static final String TITLE = Text.colorize(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_misc_gui.title"));
	private static final int LINES = WildPrisonAutoMiner.getInstance().getConfig().get().getInt("level_up_misc_gui.lines");

	private static final ItemStack EMPTY_SLOT_ITEM = ItemStackBuilder.
			of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_misc_gui.empty_slots").split(":")[0]))
			.data(Integer.parseInt(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_misc_gui.empty_slots").split(":")[1])).build();

	private static final ItemStack HELP_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_misc_gui.help_item.material")))
			.name(WildPrisonAutoMiner.getInstance().getConfig().get().getString("level_up_misc_gui.help_item.name")).lore(WildPrisonAutoMiner.getInstance().getConfig().get().getStringList("level_up_misc_gui.help_item.lore")).build();

	private static int HELP_ITEM_SLOT = WildPrisonAutoMiner.getInstance().getConfig().get().getInt("level_up_misc_gui.help_item.slot");

	public LevelMiscAutoMinerGui(Player player) {
		super(player, LINES, TITLE);

	}

	@Override
	public void redraw() {
		if (isFirstDraw()) {

			for (int i = 0; i < this.getHandle().getSize(); i++) {
				this.setItem(i, ItemStackBuilder.of(EMPTY_SLOT_ITEM).buildItem().build());
			}

			this.setItem(HELP_ITEM_SLOT, ItemStackBuilder.of(HELP_ITEM).buildItem().build());
		}
	}
}
