package me.drawethree.ultraprisoncore.enchants.gui;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.text.Text;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantGUI extends Gui {


    private static String GUI_TITLE = Text.colorize(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.title"));
    private static Item EMPTY_SLOT_ITEM = ItemStackBuilder.
            of(CompMaterial.fromString(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.empty_slots")).toItem()).buildItem().build();

    private static int PICKAXE_ITEM_SLOT = UltraPrisonEnchants.getInstance().getConfig().get().getInt("enchant_menu.pickaxe_slot");
    private static int HELP_ITEM_SLOT = UltraPrisonEnchants.getInstance().getConfig().get().getInt("enchant_menu.help_item.slot");
    private static int DISENCHANT_ITEM_SLOT = UltraPrisonEnchants.getInstance().getConfig().get().getInt("enchant_menu.disenchant_item.slot");

    private static int GUI_LINES = UltraPrisonEnchants.getInstance().getConfig().get().getInt("enchant_menu.lines");

    private static Item HELP_ITEM = ItemStackBuilder.of(CompMaterial.fromString(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.help_item.material")).toMaterial())
            .name(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.help_item.name")).lore(UltraPrisonEnchants.getInstance().getConfig().get().getStringList("enchant_menu.help_item.lore")).buildItem().build();
    private static ItemStack DISENCHANT_ITEM = ItemStackBuilder.of(CompMaterial.fromString(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.disenchant_item.material")).toMaterial())
            .name(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.disenchant_item.name")).lore(UltraPrisonEnchants.getInstance().getConfig().get().getStringList("enchant_menu.disenchant_item.lore")).build();

    @Getter
    @Setter
    private ItemStack pickAxe;

    @Getter
    private int pickaxePlayerInventorySlot;

    public EnchantGUI(Player player, ItemStack pickAxe, int pickaxePlayerInventorySlot) {
        super(player, GUI_LINES, GUI_TITLE);

        this.pickAxe = pickAxe;
        this.pickaxePlayerInventorySlot = pickaxePlayerInventorySlot;

        /*Events.subscribe(InventoryCloseEvent.class, EventPriority.LOWEST)
                .filter(e -> e.getInventory().equals(this.getHandle()))
                .handler(e -> {
                    ((Player) e.getPlayer()).updateInventory();
                }).bindWith(this);
        */

        /*Events.subscribe(PlayerTeleportEvent.class, EventPriority.LOWEST)
                .filter(e -> e.getPlayer().getUniqueId().equals(this.getPlayer().getUniqueId()))
                .handler(e -> {
                    //List<IWrappedRegion> regions = WorldGuardWrapper.getInstance().getRegions(e.getFrom()).stream().filter(reg -> reg.getId().toLowerCase().startsWith("mine")).collect(Collectors.toList());

                    this.getPlayer().closeInventory();
                }).bindWith(this);
        */

        /*if (UltraPrisonCore.getInstance().getJetsPrisonMinesAPI() != null) {

            Events.subscribe(MinePreResetEvent.class)
                    .handler(e -> {
                        if (e.getMine().isLocationInRegion(this.getPlayer().getLocation())) {
                            this.getPlayer().closeInventory();
                        }
                    }).bindWith(this);
        }*/

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
        DISENCHANT_ITEM = ItemStackBuilder.of(CompMaterial.fromString(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.disenchant_item.material")).toMaterial())
                .name(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.disenchant_item.name")).lore(UltraPrisonEnchants.getInstance().getConfig().get().getStringList("enchant_menu.disenchant_item.lore")).build();
        HELP_ITEM = ItemStackBuilder.of(CompMaterial.fromString(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.help_item.material")).toMaterial())
                .name(UltraPrisonEnchants.getInstance().getConfig().get().getString("enchant_menu.help_item.name")).lore(UltraPrisonEnchants.getInstance().getConfig().get().getStringList("enchant_menu.help_item.lore")).buildItem().build();
        HELP_ITEM_SLOT = UltraPrisonEnchants.getInstance().getConfig().get().getInt("enchant_menu.help_item.slot");
        PICKAXE_ITEM_SLOT = UltraPrisonEnchants.getInstance().getConfig().get().getInt("enchant_menu.pickaxe_slot");
    }
}
