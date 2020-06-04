package me.drawethree.wildprisoncore.autominer.gui;

import me.drawethree.wildprisoncore.autominer.WildPrisonAutoMiner;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MainAutoMinerGui extends Gui {

    private static final String TITLE = Text.colorize(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.title"));
    private static final int LINES = WildPrisonAutoMiner.getInstance().getConfig().get().getInt("main_gui.lines");

    private static final Item EMPTY_SLOT_ITEM = ItemStackBuilder.
            of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.empty_slots").split(":")[0]))
            .data(Integer.parseInt(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.empty_slots").split(":")[1])).buildItem().build();

    private static final Item HELP_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.help_item.material")))
            .name(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.help_item.name")).lore(WildPrisonAutoMiner.getInstance().getConfig().get().getStringList("main_gui.help_item.lore")).buildItem().build();

    private static final Item LEVEL_UP_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.level_up_item.material")))
            .name(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.level_up_item.name")).lore(WildPrisonAutoMiner.getInstance().getConfig().get().getStringList("main_gui.level_up_item.lore")).buildItem().build();

    private static final Item LEVEL_UP_MISC_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.level_up_misc_item.material")))
            .name(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.level_up_misc_item.name")).lore(WildPrisonAutoMiner.getInstance().getConfig().get().getStringList("main_gui.level_up_misc_item.lore")).buildItem().build();

    private static final Item AUTOMINER_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.autominer_item.material")))
            .name(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.autominer_item.name")).lore(WildPrisonAutoMiner.getInstance().getConfig().get().getStringList("main_gui.autominer_item.lore")).buildItem().build();

    private static int LEVEL_UP_ITEM_SLOT = WildPrisonAutoMiner.getInstance().getConfig().get().getInt("main_gui.level_up_item.slot");
    private static int AUTOMINER_ITEM_SLOT = WildPrisonAutoMiner.getInstance().getConfig().get().getInt("main_gui.autominer_item.slot");
    private static int LEVEL_UP_MISC_ITEM_SLOT = WildPrisonAutoMiner.getInstance().getConfig().get().getInt("main_gui.level_up_misc_item.slot");
    private static int HELP_ITEM_SLOT = WildPrisonAutoMiner.getInstance().getConfig().get().getInt("main_gui.help_item.slot");


    public MainAutoMinerGui(Player player) {
        super(player, LINES, TITLE);
    }

    @Override
    public void redraw() {
        if (isFirstDraw()) {

            for (int i = 0; i < this.getHandle().getSize(); i++) {
                this.setItem(i, EMPTY_SLOT_ITEM);
            }

            this.setItem(LEVEL_UP_ITEM_SLOT, LEVEL_UP_ITEM);
            this.setItem(LEVEL_UP_MISC_ITEM_SLOT, LEVEL_UP_MISC_ITEM);
            this.setItem(AUTOMINER_ITEM_SLOT, AUTOMINER_ITEM);
            this.setItem(HELP_ITEM_SLOT, HELP_ITEM);
        }
    }
}
