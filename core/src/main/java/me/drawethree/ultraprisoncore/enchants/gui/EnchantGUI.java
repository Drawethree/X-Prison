package me.drawethree.ultraprisoncore.enchants.gui;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.drawethree.ultraprisoncore.utils.SkullUtils;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantGUI extends Gui {


    private static String GUI_TITLE;
    private static Item EMPTY_SLOT_ITEM;

    private static int PICKAXE_ITEM_SLOT;
    private static int HELP_ITEM_SLOT;
    private static int DISENCHANT_ITEM_SLOT;

    private static int GUI_LINES;

    private static Item HELP_ITEM;
    private static ItemStack DISENCHANT_ITEM;

    static {
        reload();
    }

    @Getter
    @Setter
    private ItemStack pickAxe;

    @Getter
    private int pickaxePlayerInventorySlot;

    public EnchantGUI(Player player, ItemStack pickAxe, int pickaxePlayerInventorySlot) {
        super(player, GUI_LINES, GUI_TITLE);

        this.pickAxe = pickAxe;
        this.pickaxePlayerInventorySlot = pickaxePlayerInventorySlot;

        Events.subscribe(InventoryCloseEvent.class, EventPriority.LOWEST)
                .filter(e -> e.getInventory().equals(this.getHandle()))
                .handler(e -> {
                    UltraPrisonCore.getInstance().getEnchants().getEnchantsManager().handlePickaxeUnequip(this.getPlayer(),this.pickAxe);
                    UltraPrisonCore.getInstance().getEnchants().getEnchantsManager().handlePickaxeEquip(this.getPlayer(),this.pickAxe);
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
            this.setItem(DISENCHANT_ITEM_SLOT, ItemStackBuilder.of(DISENCHANT_ITEM).build(() -> {
                this.close();
                new DisenchantGUI(this.getPlayer(), this.pickAxe, this.pickaxePlayerInventorySlot).open();
            }));
        }

        for (UltraPrisonEnchantment enchantment : UltraPrisonEnchantment.all()) {
            if (!enchantment.isEnabled()) {
                continue;
            }
            int level = UltraPrisonEnchants.getInstance().getEnchantsManager().getEnchantLevel(this.pickAxe, enchantment.getId());
            this.setItem(enchantment.getGuiSlot(), UltraPrisonEnchants.getInstance().getEnchantsManager().getGuiItem(enchantment, this, level));
        }

        this.setItem(PICKAXE_ITEM_SLOT, Item.builder(this.pickAxe).build());
    }

    public static void reload() {
        GUI_TITLE = Text.colorize(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.title"));
        EMPTY_SLOT_ITEM = ItemStackBuilder.
                of(CompMaterial.fromString(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.empty_slots")).toItem()).buildItem().build();
        PICKAXE_ITEM_SLOT = UltraPrisonEnchants.getInstance().getConfig().get().getInt("enchant_menu.pickaxe_slot");
        GUI_LINES = UltraPrisonEnchants.getInstance().getConfig().get().getInt("enchant_menu.lines");


        String base64 = UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.disenchant_item.Base64", null);

        if (base64 != null) {
            DISENCHANT_ITEM = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(base64))
                    .name(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.disenchant_item.name")).lore(UltraPrisonEnchants.getInstance().getConfig().get().getStringList("enchant_menu.disenchant_item.lore")).build();
        } else {
            DISENCHANT_ITEM = ItemStackBuilder.of(CompMaterial.fromString(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.disenchant_item.material")).toMaterial())
                    .name(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.disenchant_item.name")).lore(UltraPrisonEnchants.getInstance().getConfig().get().getStringList("enchant_menu.disenchant_item.lore")).build();
        }

        base64 = UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.help_item.Base64", null);

        if (base64 != null) {
            HELP_ITEM = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(base64))
                    .name(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.help_item.name")).lore(UltraPrisonEnchants.getInstance().getConfig().get().getStringList("enchant_menu.help_item.lore")).buildItem().build();
        } else {
            HELP_ITEM = ItemStackBuilder.of(CompMaterial.fromString(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.help_item.material")).toMaterial())
                    .name(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.help_item.name")).lore(UltraPrisonEnchants.getInstance().getConfig().get().getStringList("enchant_menu.help_item.lore")).buildItem().build();
        }


        HELP_ITEM_SLOT = UltraPrisonEnchants.getInstance().getConfig().get().getInt("enchant_menu.help_item.slot");
        PICKAXE_ITEM_SLOT = UltraPrisonEnchants.getInstance().getConfig().get().getInt("enchant_menu.pickaxe_slot");
        DISENCHANT_ITEM_SLOT = UltraPrisonEnchants.getInstance().getConfig().get().getInt("enchant_menu.disenchant_item.slot");
    }
}
