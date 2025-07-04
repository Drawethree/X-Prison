package dev.drawethree.xprison.mainmenu.modules;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModuleBase;
import dev.drawethree.xprison.api.XPrisonModule;
import dev.drawethree.xprison.mainmenu.confirmation.ReloadModuleConfirmationGui;
import dev.drawethree.xprison.mainmenu.confirmation.ResetModulePlayerDataConfirmationGui;
import dev.drawethree.xprison.utils.Constants;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import dev.drawethree.xprison.utils.misc.SkullUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModulesGui extends Gui {

    private static final MenuScheme LAYOUT_WHITE = new MenuScheme()
            .mask("011111110")
            .mask("110000011")
            .mask("100000001")
            .mask("110000011")
            .mask("011111110");

    private static final MenuScheme LAYOUT_RED = new MenuScheme()
            .mask("100000001")
            .mask("000000000")
            .mask("000000000")
            .mask("000000000")
            .mask("100000001");

    private static final List<GuiModule> MODULES = new ArrayList<>();

    static {
        MODULES.add(new GuiModule(XPrison.getInstance().getAutoMiner(),() -> ItemStackBuilder.of(XMaterial.DIAMOND_PICKAXE.get()).name("&e&lAutoMiner").lore(getLore(XPrison.getInstance().getAutoMiner())).build(),11));
        MODULES.add(new GuiModule(XPrison.getInstance().getAutoSell(),() -> ItemStackBuilder.of(SkullUtils.MONEY_SKULL.clone()).name("&e&lAutoSell").lore(getLore(XPrison.getInstance().getAutoSell())).build(),12));
        MODULES.add(new GuiModule(XPrison.getInstance().getEnchants(),() -> ItemStackBuilder.of(XMaterial.ENCHANTED_BOOK.parseItem()).name("&e&lEnchants").lore(getLore(XPrison.getInstance().getEnchants())).build(),13));
        MODULES.add(new GuiModule(XPrison.getInstance().getGangs(),() -> ItemStackBuilder.of(SkullUtils.GANG_SKULL.clone()).name("&e&lGangs").lore(getLore(XPrison.getInstance().getGangs())).build(),14));
        MODULES.add(new GuiModule(XPrison.getInstance().getGems(),() -> ItemStackBuilder.of(XMaterial.EMERALD.get()).name("&e&lGems").lore(getLore(XPrison.getInstance().getGems())).build(),15));
        MODULES.add(new GuiModule(XPrison.getInstance().getRanks(),() -> ItemStackBuilder.of(SkullUtils.DIAMOND_R_SKULL.clone()).name("&e&lRanks").lore(getLore(XPrison.getInstance().getRanks())).build(),19));
        MODULES.add(new GuiModule(XPrison.getInstance().getPrestiges(),() -> ItemStackBuilder.of(SkullUtils.DIAMOND_P_SKULL.clone()).name("&e&lPrestiges").lore(getLore(XPrison.getInstance().getPrestiges())).build(),20));
        MODULES.add(new GuiModule(XPrison.getInstance().getPickaxeLevels(),() -> ItemStackBuilder.of(XMaterial.EXPERIENCE_BOTTLE.parseItem()).name("&e&lPickaxe Levels").lore(getLore(XPrison.getInstance().getPickaxeLevels())).build(),21));
        MODULES.add(new GuiModule(XPrison.getInstance().getTokens(),() -> ItemStackBuilder.of(SkullUtils.COIN_SKULL.clone()).name("&e&lTokens").lore(getLore(XPrison.getInstance().getTokens())).build(),22));
        MODULES.add(new GuiModule(XPrison.getInstance().getMultipliers(),() -> ItemStackBuilder.of(XMaterial.GOLD_INGOT.get()).name("&e&lMultipliers").lore(getLore(XPrison.getInstance().getMultipliers())).build(),23));
        MODULES.add(new GuiModule(XPrison.getInstance().getMines(),() -> ItemStackBuilder.of(XMaterial.DIAMOND_ORE.get()).name("&e&lMines").lore(getLore(XPrison.getInstance().getMines())).build(),24));
        MODULES.add(new GuiModule(XPrison.getInstance().getBombs(),() -> ItemStackBuilder.of(XMaterial.TNT.parseItem()).name("&e&lBombs").lore(getLore(XPrison.getInstance().getBombs())).build(),25));
        MODULES.add(new GuiModule(XPrison.getInstance().getHistory(),() -> ItemStackBuilder.of(XMaterial.BOOK.parseItem()).name("&e&lHistory").lore(getLore(XPrison.getInstance().getHistory())).build(),29));
    }

    public ModulesGui(Player player) {
        super(player, 5, "X-Prison - Module Manager");
    }

    @Override
    public void redraw() {
        if (isFirstDraw()) {
            this.drawDefaultItems();
        }

        for (GuiModule reloadable : MODULES) {
            this.setItem(reloadable.getSlot(), ItemStackBuilder.of(reloadable.getIcon()).clearLore().lore(getLore(reloadable.getModule())).buildItem().bind(inventoryClickEvent -> {
                if (inventoryClickEvent.getClick() == ClickType.LEFT) {
                    openReloadModuleGui(reloadable.getModule());
                } else if (inventoryClickEvent.getClick() == ClickType.RIGHT) {
                    openResetPlayerDataGui(reloadable.getModule());
                } else if (inventoryClickEvent.getClick() == ClickType.SHIFT_LEFT) {
                    reloadable.toggleEnable(getPlayer());
                }
                redraw();
            },ClickType.LEFT, ClickType.RIGHT, ClickType.SHIFT_LEFT).build());
        }
    }

    private void openReloadModuleGui(XPrisonModuleBase module) {
        ReloadModuleConfirmationGui reloadModuleConfirmationGui = new ReloadModuleConfirmationGui(getPlayer(), module);
        reloadModuleConfirmationGui.setFallbackGui(player -> this);
        reloadModuleConfirmationGui.open();
    }

    private void openResetPlayerDataGui(XPrisonModuleBase module) {
        ResetModulePlayerDataConfirmationGui resetModulePlayerDataConfirmationGui = new ResetModulePlayerDataConfirmationGui(getPlayer(), module);
        resetModulePlayerDataConfirmationGui.setFallbackGui(player -> this);
        resetModulePlayerDataConfirmationGui.open();
    }

    private void drawDefaultItems() {
        MenuPopulator populator = LAYOUT_WHITE.newPopulator(this);

        while (populator.hasSpace()) {
            populator.accept(ItemStackBuilder.of(XMaterial.WHITE_STAINED_GLASS_PANE.parseItem()).name(" ").buildItem().build());
        }

        populator = LAYOUT_RED.newPopulator(this);

        while (populator.hasSpace()) {
            populator.accept(ItemStackBuilder.of(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).name(" ").buildItem().build());
        }

        this.setItem(36, ItemStackBuilder.of(Material.BARRIER).name("&c&lClose").lore("&7Click to close the gui.").build(this::close));
        this.setItem(44, ItemStackBuilder.of(SkullUtils.HELP_SKULL.clone()).name("&e&lNeed more help?").lore("&7Right-Click to see plugin's Wiki", "&7Left-Click to join Discord Support.")
                .build(() -> {
                    this.close();
                    PlayerUtils.sendMessage(this.getPlayer(), " ");
                    PlayerUtils.sendMessage(this.getPlayer(), "&eX-Prison - Wiki");
                    PlayerUtils.sendMessage(this.getPlayer(), "&7https://github.com/Drawethree/X-Prison/wiki");
                    PlayerUtils.sendMessage(this.getPlayer(), " ");
                }, () -> {
                    this.close();
                    PlayerUtils.sendMessage(this.getPlayer(), " ");
                    PlayerUtils.sendMessage(this.getPlayer(), "&eX-Prison - Discord");
                    PlayerUtils.sendMessage(this.getPlayer(), "&7" + Constants.DISCORD_LINK);
                    PlayerUtils.sendMessage(this.getPlayer(), " ");
                }));
    }

    private static List<String> getLore(XPrisonModule module) {
        return Arrays.asList(
                "&7Click actions:",
                "&eLeft-Click &7to reload",
                "&eRight-Click &7to reset player data",
                "&eSHIFT Left-Click &7to enable/disable",
                "",
                "&7Status: " + (module.isEnabled() ? "&aEnabled" : "&cDisabled")
        );
    }
}
