package me.drawethree.ultraprisoncore.enchants.gui;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
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

public class DisenchantGUI extends Gui {

    private static final String GUI_TITLE = Text.colorize(UltraPrisonEnchants.getInstance().getConfig().get().getString("disenchant_menu.title"));
    private static final Item EMPTY_SLOT_ITEM = ItemStackBuilder.
            of(Material.valueOf(UltraPrisonEnchants.getInstance().getConfig().get().getString("disenchant_menu.empty_slots").split(":")[0]))
            .data(Integer.parseInt(UltraPrisonEnchants.getInstance().getConfig().get().getString("disenchant_menu.empty_slots").split(":")[1])).buildItem().build();

    private static final Item HELP_ITEM = ItemStackBuilder.of(Material.valueOf(UltraPrisonEnchants.getInstance().getConfig().get().getString("disenchant_menu.help_item.material")))
            .name(UltraPrisonEnchants.getInstance().getConfig().get().getString("disenchant_menu.help_item.name")).lore(UltraPrisonEnchants.getInstance().getConfig().get().getStringList("disenchant_menu.help_item.lore")).buildItem().build();

    private static int HELP_ITEM_SLOT = UltraPrisonEnchants.getInstance().getConfig().get().getInt("disenchant_menu.help_item.slot");
    private static int PICKAXE_ITEM_SLOT = UltraPrisonEnchants.getInstance().getConfig().get().getInt("disenchant_menu.pickaxe_slot");
    private static int GUI_LINES = UltraPrisonEnchants.getInstance().getConfig().get().getInt("disenchant_menu.lines");


    @Getter
    @Setter
    private ItemStack pickAxe;

    public DisenchantGUI(Player player, ItemStack pickAxe) {
        super(player, GUI_LINES, GUI_TITLE);

        this.pickAxe = pickAxe;

        Events.subscribe(InventoryCloseEvent.class)
                .filter(e -> e.getInventory().equals(this.getHandle()))
                .handler(e -> {
                    if (!this.getPlayer().getItemInHand().equals(this.pickAxe)) {
                        this.getPlayer().getInventory().remove(this.pickAxe);
                    }
                    ItemStack inHand = this.getPlayer().getItemInHand();
                    this.getPlayer().setItemInHand(this.pickAxe);

                    if (inHand != null) {
                        this.getPlayer().getInventory().addItem(inHand);
                    }

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

            this.setItem(HELP_ITEM_SLOT, HELP_ITEM);
        }

        for (UltraPrisonEnchantment enchantment : UltraPrisonEnchantment.all()) {
            /*if (!enchantment.isRefundEnabled()) {
                continue;
            }*/
            int level = UltraPrisonEnchants.getInstance().getEnchantsManager().getEnchantLevel(this.pickAxe, enchantment.getId());
            this.setItem(enchantment.refundGuiSlot(), UltraPrisonEnchants.getInstance().getEnchantsManager().getRefundGuiItem(enchantment, this, level));
        }

        this.setItem(PICKAXE_ITEM_SLOT, Item.builder(pickAxe).build());
        //this.getPlayer().setItemInHand(pickAxe);
    }
}
