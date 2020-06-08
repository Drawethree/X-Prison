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

public class MainAutoMinerGui extends Gui {

    private static final String TITLE = Text.colorize(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.title"));
    private static final int LINES = WildPrisonAutoMiner.getInstance().getConfig().get().getInt("main_gui.lines");

    private static final ItemStack EMPTY_SLOT_ITEM = ItemStackBuilder.
            of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.empty_slots").split(":")[0]))
            .data(Integer.parseInt(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.empty_slots").split(":")[1])).build();

    private static final ItemStack HELP_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.help_item.material")))
            .name(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.help_item.name")).lore(WildPrisonAutoMiner.getInstance().getConfig().get().getStringList("main_gui.help_item.lore")).build();

    private static final ItemStack LEVEL_UP_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.level_up_item.material")))
            .name(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.level_up_item.name")).lore(WildPrisonAutoMiner.getInstance().getConfig().get().getStringList("main_gui.level_up_item.lore")).build();

    private static final ItemStack LEVEL_UP_MISC_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.level_up_misc_item.material")))
            .name(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.level_up_misc_item.name")).lore(WildPrisonAutoMiner.getInstance().getConfig().get().getStringList("main_gui.level_up_misc_item.lore")).build();

    private static final ItemStack AUTOMINER_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.autominer_item.material")))
            .name(WildPrisonAutoMiner.getInstance().getConfig().get().getString("main_gui.autominer_item.name")).lore(WildPrisonAutoMiner.getInstance().getConfig().get().getStringList("main_gui.autominer_item.lore")).build();

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
                this.setItem(i, ItemStackBuilder.of(EMPTY_SLOT_ITEM).buildItem().build());
            }

            this.setItem(LEVEL_UP_ITEM_SLOT, ItemStackBuilder.of(LEVEL_UP_ITEM).build(() -> {
                this.close();
                new LevelAutoMinerGui(this.getPlayer()).open();
            }));
            this.setItem(LEVEL_UP_MISC_ITEM_SLOT, ItemStackBuilder.of(LEVEL_UP_MISC_ITEM).build(() -> {
                this.close();
                new LevelCommandAutoMinerGui(this.getPlayer()).open();
            }));
            this.setItem(AUTOMINER_ITEM_SLOT, ItemStackBuilder.of(this.getAutominerItem()).buildItem().build());
            this.setItem(HELP_ITEM_SLOT, ItemStackBuilder.of(HELP_ITEM).buildItem().build());
        }
    }


    private ItemStack getAutominerItem() {
        ItemStack item = AUTOMINER_ITEM.clone();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, lore.get(i)
                    .replace("%level%", String.format("%,d", WildPrisonAutoMiner.getInstance().getAutoMinerFuelLevel(this.getPlayer()).getLevel()))
                    .replace("%rewards_level%", String.format("%,d", WildPrisonAutoMiner.getInstance().getPlayerCommandLevel(this.getPlayer())))
                    .replace("%fuel%", String.format("%,d", WildPrisonAutoMiner.getInstance().getPlayerFuel(this.getPlayer()))));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
