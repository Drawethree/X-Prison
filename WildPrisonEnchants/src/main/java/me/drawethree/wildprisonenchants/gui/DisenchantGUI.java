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

public class DisenchantGUI extends Gui {

    private static final String GUI_TITLE = Text.colorize(WildPrisonEnchants.getInstance().getConfig().getString("disenchant_menu.title"));
    private static final Item EMPTY_SLOT_ITEM = ItemStackBuilder.
            of(Material.valueOf(WildPrisonEnchants.getInstance().getConfig().getString("disenchant_menu.empty_slots").split(":")[0]))
            .data(Integer.parseInt(WildPrisonEnchants.getInstance().getConfig().getString("disenchant_menu.empty_slots").split(":")[1])).buildItem().build();

    private static final Item HELP_ITEM = ItemStackBuilder.of(Material.valueOf(WildPrisonEnchants.getInstance().getConfig().getString("disenchant_menu.help_item.material")))
            .name(WildPrisonEnchants.getInstance().getConfig().getString("disenchant_menu.help_item.name")).lore(WildPrisonEnchants.getInstance().getConfig().getStringList("disenchant_menu.help_item.lore")).buildItem().build();

    private static int HELP_ITEM_SLOT = WildPrisonEnchants.getInstance().getConfig().getInt("disenchant_menu.help_item.slot");
    private static int PICKAXE_ITEM_SLOT = WildPrisonEnchants.getInstance().getConfig().getInt("disenchant_menu.pickaxe_slot");

    @Getter
    @Setter
    private ItemStack pickAxe;

    public DisenchantGUI(Player player, ItemStack pickAxe) {
        super(player, 6, GUI_TITLE);
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

            this.setItem(HELP_ITEM_SLOT, HELP_ITEM);
        }

        for (WildPrisonEnchantment enchantment : WildPrisonEnchantment.all()) {
            if (!enchantment.isRefundEnabled()) {
                continue;
            }
            int level = WildPrisonEnchants.getEnchantsManager().getEnchantLevel(this.pickAxe, enchantment.getId());
            this.setItem(enchantment.refundGuiSlot(), (enchantment.getRefundGuiItem(this, level)));
        }

        this.setItem(PICKAXE_ITEM_SLOT, Item.builder(pickAxe).build());
        this.getPlayer().setItemInHand(pickAxe);
    }
}
