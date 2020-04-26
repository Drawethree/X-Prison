package me.drawethree.wildprisonenchants.gui;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantGUI extends Gui {


    private static final String GUI_TITLE = Text.colorize(WildPrisonEnchants.getInstance().getConfig().getString("enchant_menu.title"));
    private static final Item EMPTY_SLOT_ITEM = ItemStackBuilder.
            of(Material.valueOf(WildPrisonEnchants.getInstance().getConfig().getString("enchant_menu.empty_slots").split(":")[0]))
            .data(Integer.parseInt(WildPrisonEnchants.getInstance().getConfig().getString("enchant_menu.empty_slots").split(":")[1])).buildItem().build();

    private static int PICKAXE_ITEM_SLOT = WildPrisonEnchants.getInstance().getConfig().getInt("enchant_menu.pickaxe_slot");

    private static int GUI_LINES = WildPrisonEnchants.getInstance().getConfig().getInt("enchant_menu.lines");

    @Getter
    @Setter
    private ItemStack pickAxe;

    public EnchantGUI(Player player, ItemStack pickAxe) {
        super(player, GUI_LINES, GUI_TITLE);
        this.pickAxe = pickAxe;

        Events.subscribe(InventoryCloseEvent.class)
                .filter(e -> e.getInventory().equals(this.getHandle()))
                .handler(e -> {
                    e.getPlayer().setItemInHand(this.pickAxe);
                }).bindWith(this);
    }


    @Override
    public void redraw() {

        // perform initial setup.
        if (isFirstDraw()) {
            for (int i = 0; i < 54; i++) {
                this.setItem(i, EMPTY_SLOT_ITEM);
            }
        }

        for (WildPrisonEnchantment enchantment : WildPrisonEnchantment.all()) {
            if (!enchantment.isEnabled()) {
                continue;
            }
            int level = WildPrisonEnchants.getEnchantsManager().getEnchantLevel(this.pickAxe, enchantment.getId());
            this.setItem(enchantment.getGuiSlot(), WildPrisonEnchants.getEnchantsManager().getGuiItem(enchantment, this, level));
        }

        this.setItem(PICKAXE_ITEM_SLOT, Item.builder(pickAxe).build());
        this.getPlayer().setItemInHand(pickAxe);
    }
}
