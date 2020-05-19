package me.drawethree.wildprisoncore.enchants.gui;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.WildPrisonEnchantment;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantGUI extends Gui {


    private static final String GUI_TITLE = Text.colorize(WildPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.title"));
    private static final Item EMPTY_SLOT_ITEM = ItemStackBuilder.
            of(Material.valueOf(WildPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.empty_slots").split(":")[0]))
            .data(Integer.parseInt(WildPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.empty_slots").split(":")[1])).buildItem().build();

    private static int PICKAXE_ITEM_SLOT = WildPrisonEnchants.getInstance().getConfig().get().getInt("enchant_menu.pickaxe_slot");

    private static int GUI_LINES = WildPrisonEnchants.getInstance().getConfig().get().getInt("enchant_menu.lines");

    @Getter
    @Setter
    private ItemStack pickAxe;

    public EnchantGUI(Player player, ItemStack pickAxe) {
        super(player, GUI_LINES, GUI_TITLE);

        this.pickAxe = pickAxe;

        Events.subscribe(InventoryCloseEvent.class)
                .filter(e -> e.getInventory().equals(this.getHandle()))
                .handler(e -> {
                    if (!this.getPlayer().getItemInHand().equals(this.pickAxe)) {
                        this.getPlayer().getInventory().remove(this.pickAxe);
                    }
                    this.getPlayer().setItemInHand(this.pickAxe);
                    Schedulers.async().runLater(() -> {
                        ((Player) e.getPlayer()).updateInventory();
                    }, 5);
                }).bindWith(this);
    }


    @Override
    public void redraw() {

        // perform initial setup.
        if (isFirstDraw()) {
            for (int i = 0; i < this.getHandle().getSize(); i++) {
                this.setItem(i, EMPTY_SLOT_ITEM);
            }
        }

        for (WildPrisonEnchantment enchantment : WildPrisonEnchantment.all()) {
            if (!enchantment.isEnabled()) {
                continue;
            }
            int level = WildPrisonEnchants.getInstance().getEnchantsManager().getEnchantLevel(this.pickAxe, enchantment.getId());
            this.setItem(enchantment.getGuiSlot(), WildPrisonEnchants.getInstance().getEnchantsManager().getGuiItem(enchantment, this, level));
        }

        this.setItem(PICKAXE_ITEM_SLOT, Item.builder(pickAxe).build());
        //this.getPlayer().setItemInHand(pickAxe);
    }
}
